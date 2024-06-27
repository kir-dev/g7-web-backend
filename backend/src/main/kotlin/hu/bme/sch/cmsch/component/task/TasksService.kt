package hu.bme.sch.cmsch.component.task

import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.extending.TaskSubmissionListener
import hu.bme.sch.cmsch.model.RoleType
import hu.bme.sch.cmsch.model.UserEntity
import hu.bme.sch.cmsch.repository.GroupRepository
import hu.bme.sch.cmsch.repository.UserRepository
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.TimeService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.sql.SQLException
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
@ConditionalOnBean(TaskComponent::class)
open class TasksService(
    private val taskRepository: TaskEntityRepository,
    private val submitted: SubmittedTaskRepository,
    private val categories: TaskCategoryRepository,
    private val clock: TimeService,
    private val taskComponent: TaskComponent,
    private val listeners: List<TaskSubmissionListener>,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val storageService: StorageService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val target = "task"
    }

    @Transactional(readOnly = true)
    open fun getById(id: Int): Optional<TaskEntity> {
        return taskRepository.findById(id)
    }

    @Transactional(readOnly = true)
    open fun getSubmissionForUserOrNull(user: CmschUser, task: Optional<TaskEntity>): SubmittedTaskEntity? {
        if (task.isEmpty)
            return null

        return submitted.findByTask_IdAndUserId(task.get().id, user.id)
            .orElse(null)
    }

    @Transactional(readOnly = true)
    open fun getSubmissionForGroupOrNull(groupId: Int, task: Optional<TaskEntity>): SubmittedTaskEntity? {
        if (task.isEmpty)
            return null

        return submitted.findByTask_IdAndGroupId(task.get().id, groupId)
                .orElse(null)
    }

    @Transactional(readOnly = true)
    open fun getHighlightedOnes(groupId: Int): List<TaskEntityWrapperDto> {
        return taskRepository.findAllByHighlightedTrueAndVisibleTrue()
                .map { findSubmissionStatusForGroup(it, groupId) }
    }

    @Transactional(readOnly = true)
    open fun getAllTasks() = taskRepository.findAllByVisibleTrue()

    @Transactional(readOnly = true)
    open fun getAllTasksNameView() = taskRepository.findAllTaskNameView()

    @Transactional(readOnly = true)
    open fun getAllTasksForGuests(): List<TaskEntityWrapperDto> {
        return taskRepository.findAllByVisibleTrue()
                .map { TaskEntityWrapperDto(it, TaskStatus.NOT_LOGGED_IN, "") }
    }

    @Transactional(readOnly = true)
    open fun getAllTasksForGroup(groupId: Int, categoryId: Int): List<TaskEntityWrapperDto> {
        val allTasks = taskRepository.findAllByVisibleTrueAndCategoryId(categoryId)
        val taskIds = allTasks.map { it.id }
        val submissions = submitted.findAllByTask_IdInAndGroupId(taskIds, groupId).associateBy { it.task?.id ?: 0 }
        return allTasks.map { findSubmission(submissions[it.id], it) }
    }

    @Transactional(readOnly = true)
    open fun getAllTasksForUser(user: CmschUser, categoryId: Int): List<TaskEntityWrapperDto> {
        val allTasks = taskRepository.findAllByVisibleTrueAndCategoryId(categoryId)
        val taskIds = allTasks.map { it.id }
        val submissions = submitted.findAllByTask_IdInAndUserId(taskIds, user.id).associateBy { it.task?.id ?: 0 }
        return allTasks.map { findSubmission(submissions[it.id], it) }
    }

    private fun findSubmissionStatusForGroup(taskEntity: TaskEntity, groupId: Int): TaskEntityWrapperDto {
        try {
            val submission = submitted.findByTask_IdAndGroupId(taskEntity.id, groupId).getOrNull()
            return findSubmission(submission, taskEntity)
        } catch (e: Exception) {
            log.error("Failed to fetch TASK_SUBMISSION: {} for group {}", taskEntity.title, groupId, e)
            throw e
        }
    }

    private fun findSubmissionStatusForUser(taskEntity: TaskEntity, user: CmschUser): TaskEntityWrapperDto {
        try {
            val submission = submitted.findByTask_IdAndUserId(taskEntity.id, user.id).getOrNull()
            return findSubmission(submission, taskEntity)
        } catch (e: Exception) {
            log.error("Failed to fetch TASK_SUBMISSION: {} for user {}", taskEntity.title, user.id, e)
            throw e
        }
    }

    private fun findSubmission(submission: SubmittedTaskEntity?, taskEntity: TaskEntity): TaskEntityWrapperDto {
        return when {
            submission == null -> TaskEntityWrapperDto(taskEntity, TaskStatus.NOT_SUBMITTED, "")
            submission.rejected -> TaskEntityWrapperDto(taskEntity, TaskStatus.REJECTED, submission.response)
            submission.approved -> TaskEntityWrapperDto(taskEntity, TaskStatus.ACCEPTED, submission.response)
            else -> TaskEntityWrapperDto(taskEntity, TaskStatus.SUBMITTED, submission.response)
        }
    }

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun submitTaskReview(
        taskId: Int,
        userId: Int?,
        groupId: Int?,
        reviewMessage: String,
        isApproved: Boolean,
        score: Int,
        adminUserName: String
    ): Boolean {
        if (userId == null && groupId == null) {
            return false
        }

        val task: Optional<TaskEntity> = taskRepository.findById(taskId)
        if (!task.isPresent) {
            return false
        }

        val existingSubmission =
            if (userId == null)
                submitted.findByTask_IdAndGroupId(taskId, groupId!!)
            else
                submitted.findByTask_IdAndUserId(taskId, userId)


        val group = if (groupId != null) groupRepository.findById(groupId).getOrNull() else null
        val user = if (userId != null) userRepository.findById(userId).getOrNull() else null

        val submission: SubmittedTaskEntity = existingSubmission.getOrNull() ?: SubmittedTaskEntity(
            0, task.get(), groupId, group?.name ?: "",
            userId, user?.fullNameWithAlias ?: "",
            task.get().categoryId,
        )
        submission.apply {
            approved = isApproved
            rejected = !isApproved
            this.score = score
            response = reviewMessage
        }

        submission.addSubmissionHistory(
            date = clock.getTimeInSeconds(),
            submitterName = adminUserName,
            adminResponse = true,
            content = reviewMessage,
            status = "$score pont | értékelve",
            type = "OTHER",
            contentUrl = ""
        )
        submitted.save(submission)

        return true
    }

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun submitTaskForGroup(answer: TaskSubmissionDto, file: MultipartFile?, user: CmschUser): TaskSubmissionStatus {
        val groupId = user.groupId
            ?: return TaskSubmissionStatus.NO_ASSOCIATE_GROUP
        val task = taskRepository.findById(answer.taskId).orElse(null)
            ?: return TaskSubmissionStatus.INVALID_TASK_ID

        val now = clock.getTimeInSeconds()
        if (task.availableFrom > now || task.availableTo < now) {
            return TaskSubmissionStatus.TOO_EARLY_OR_LATE
        }
        if ((user.role.value < task.minRole.value || user.role.value > task.maxRole.value) && !user.isAdmin()) {
            log.warn("User ${user.userName} wants to access protected task '${task.title}'")
            return TaskSubmissionStatus.NO_PERMISSION
        }

        val previous = submitted.findByTask_IdAndGroupId(answer.taskId, groupId)
        if (previous.isPresent) {
            val submission = previous.get()
            if (submission.approved)
                return TaskSubmissionStatus.ALREADY_APPROVED
            if (!submission.rejected && !taskComponent.resubmissionEnabled.isValueTrue())
                return TaskSubmissionStatus.ALREADY_SUBMITTED
            return updateSubmission(user, task, answer, file, submission)

        } else {
            return newSubmission(task, answer, groupId, null, user, file)
        }
    }

    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun submitTaskForUser(answer: TaskSubmissionDto, file: MultipartFile?, user: CmschUser): TaskSubmissionStatus {
        val task = taskRepository.findById(answer.taskId).orElse(null)
            ?: return TaskSubmissionStatus.INVALID_TASK_ID

        val now = clock.getTimeInSeconds()
        if (task.availableFrom > now || task.availableTo < now) {
            return TaskSubmissionStatus.TOO_EARLY_OR_LATE
        }
        if ((user.role.value < task.minRole.value || user.role.value > task.maxRole.value) && !user.isAdmin()) {
            log.warn("User ${user.userName} wants to access protected task '${task.title}'")
            return TaskSubmissionStatus.NO_PERMISSION
        }

        val previous = submitted.findByTask_IdAndUserId(answer.taskId, user.id)
        if (previous.isPresent) {
            val submission = previous.get()
            if (submission.approved)
                return TaskSubmissionStatus.ALREADY_APPROVED
            if (!submission.rejected && !taskComponent.resubmissionEnabled.isValueTrue())
                return TaskSubmissionStatus.ALREADY_SUBMITTED
            return updateSubmission(user, task, answer, file, submission)

        } else {
            return newSubmission(task, answer, null, user.id, user, file)
        }
    }

    private fun newSubmission(
        task: TaskEntity,
        answer: TaskSubmissionDto,
        groupId: Int?,
        userId: Int?,
        user: CmschUser,
        file: MultipartFile?
    ): TaskSubmissionStatus {

        val groupName = if (groupId != null) user.groupName else null
        val userName = if (userId != null) user.userName else null

        when (task.type) {
            TaskType.TEXT -> {
                if (answer.textAnswer.isBlank())
                    return TaskSubmissionStatus.EMPTY_ANSWER

                val submission = SubmittedTaskEntity(
                    0, task, groupId, groupName ?: "",
                    userId, userName ?: "",
                    task.categoryId,
                    textAnswerLob = answer.textAnswer,
                    imageUrlAnswer = "",
                    fileUrlAnswer = "",
                    approved = false, rejected = false, score = 0
                ).addSubmissionHistory(
                    date = clock.getTimeInSeconds(),
                    submitterName = user.userName,
                    adminResponse = false,
                    content = answer.textAnswer,
                    contentUrl = "",
                    status = "0 pont | beadva",
                    type = "TEXT"
                )
                submitted.save(submission)
                listeners.forEach { it.onTaskSubmit(user, task, submission) }
                return TaskSubmissionStatus.OK

            }
            TaskType.IMAGE -> {
                if (file == null || imageFileNameInvalid(file))
                    return TaskSubmissionStatus.INVALID_IMAGE

                val fileName = storageService.saveObject(target, file)

                val submission = SubmittedTaskEntity(
                    0, task, groupId, groupName ?: "",
                    userId, userName ?: "",
                    task.categoryId,
                    textAnswerLob = "",
                    imageUrlAnswer = "$target/$fileName",
                    fileUrlAnswer = "",
                    response = "", approved = false, rejected = false, score = 0
                ).addSubmissionHistory(
                    date = clock.getTimeInSeconds(),
                    submitterName = user.userName,
                    adminResponse = false,
                    content = "",
                    contentUrl = "$target/$fileName",
                    status = "0 pont | beadva",
                    type = "IMAGE"
                )
                submitted.save(submission)
                listeners.forEach { it.onTaskSubmit(user, task, submission) }
                return TaskSubmissionStatus.OK

            }
            TaskType.BOTH -> {
                val fileName = file?.let { storageService.saveObject(target, it) }?.getOrNull() ?: ""
                if (file != null && imageFileNameInvalid(file))
                    return TaskSubmissionStatus.INVALID_IMAGE

                val submission = SubmittedTaskEntity(
                    0, task, groupId, groupName ?: "",
                    userId, userName ?: "",
                    task.categoryId,
                    textAnswerLob = answer.textAnswer,
                    imageUrlAnswer = "$target/$fileName",
                    fileUrlAnswer = "",
                    response = "", approved = false, rejected = false, score = 0
                ).addSubmissionHistory(
                    date = clock.getTimeInSeconds(),
                    submitterName = user.userName,
                    adminResponse = false,
                    content = answer.textAnswer,
                    contentUrl = "$target/$fileName",
                    status = "0 pont | beadva",
                    type = "BOTH"
                )
                submitted.save(submission)
                listeners.forEach { it.onTaskSubmit(user, task, submission) }
                return TaskSubmissionStatus.OK

            }
            TaskType.ONLY_PDF -> {
                if (file == null || pdfFileNameInvalid(file))
                    return TaskSubmissionStatus.INVALID_PDF

                val fileName = storageService.saveObject(target, file)

                val submission = SubmittedTaskEntity(
                    0, task, groupId, groupName ?: "",
                    userId, userName ?: "",
                    task.categoryId,
                    textAnswerLob = "",
                    imageUrlAnswer = "",
                    fileUrlAnswer = "$target/$fileName",
                    response = "", approved = false, rejected = false, score = 0
                ).addSubmissionHistory(
                    date = clock.getTimeInSeconds(),
                    submitterName = user.userName,
                    adminResponse = false,
                    content = "",
                    contentUrl = "$target/$fileName",
                    status = "0 pont | beadva",
                    type = "PDF"
                )
                submitted.save(submission)
                listeners.forEach { it.onTaskSubmit(user, task, submission) }
                return TaskSubmissionStatus.OK
            }
            else -> {
                throw IllegalStateException("Invalid task type: " + task.type)
            }
        }
    }

    private fun updateSubmission(
        user: CmschUser,
        task: TaskEntity,
        answer: TaskSubmissionDto,
        file: MultipartFile?,
        submission: SubmittedTaskEntity
    ): TaskSubmissionStatus {

        when (task.type) {
            TaskType.TEXT -> {
                if (answer.textAnswer.isBlank())
                    return TaskSubmissionStatus.EMPTY_ANSWER

                submission.textAnswerLob = answer.textAnswer
                submission.rejected = false
                submission.approved = false
                submission.addSubmissionHistory(
                    date = clock.getTimeInSeconds(),
                    submitterName = user.userName,
                    adminResponse = false,
                    content = answer.textAnswer,
                    status = "${submission.score} pont | beadva",
                    type = "TEXT",
                    contentUrl = ""
                )
                submitted.save(submission)
                listeners.forEach { it.onTaskUpdate(user, task, submission) }
                return TaskSubmissionStatus.OK

            }
            TaskType.IMAGE -> {
                if (file == null || imageFileNameInvalid(file))
                    return TaskSubmissionStatus.INVALID_IMAGE
                val fileName = storageService.saveObject(target, file)

                submission.imageUrlAnswer = "$target/$fileName"
                submission.rejected = false
                submission.approved = false
                submission.addSubmissionHistory(
                    date = clock.getTimeInSeconds(),
                    submitterName = user.userName,
                    adminResponse = false,
                    content = "",
                    contentUrl = "$target/$fileName",
                    status = "${submission.score} pont | beadva",
                    type = "IMAGE"
                )
                submitted.save(submission)
                listeners.forEach { it.onTaskUpdate(user, task, submission) }
                return TaskSubmissionStatus.OK

            }
            TaskType.BOTH -> {
                if (file != null && !imageFileNameInvalid(file)) {
                    val fileName = storageService.saveObject(target, file)
                    submission.imageUrlAnswer = "$target/$fileName"
                }
                submission.textAnswerLob = answer.textAnswer
                submission.rejected = false
                submission.approved = false
                submission.addSubmissionHistory(
                    date = clock.getTimeInSeconds(),
                    submitterName = user.userName,
                    adminResponse = false,
                    content = answer.textAnswer,
                    contentUrl = submission.imageUrlAnswer,
                    status = "${submission.score} pont | beadva",
                    type = "IMAGE"
                )
                submitted.save(submission)
                listeners.forEach { it.onTaskUpdate(user, task, submission) }
                return TaskSubmissionStatus.OK

            }
            TaskType.ONLY_PDF -> {
                if (file == null || pdfFileNameInvalid(file))
                    return TaskSubmissionStatus.INVALID_PDF
                val fileName = storageService.saveObject(target, file)

                submission.fileUrlAnswer = "$target/$fileName"
                submission.rejected = false
                submission.approved = false
                submission.addSubmissionHistory(
                    date = clock.getTimeInSeconds(),
                    submitterName = user.userName,
                    adminResponse = false,
                    content = "",
                    contentUrl = "$target/$fileName",
                    status = "${submission.score} pont | beadva",
                    type = "PDF"
                )
                submitted.save(submission)
                listeners.forEach { it.onTaskUpdate(user, task, submission) }
                return TaskSubmissionStatus.OK

            }
            else -> {
                throw IllegalStateException("Invalid task type: " + task.type)
            }
        }
    }

    private fun imageFileNameInvalid(file: MultipartFile): Boolean {
        return file.originalFilename == null || (
                !file.originalFilename!!.lowercase().endsWith(".png")
                        && !file.originalFilename!!.lowercase().endsWith(".jpg")
                        && !file.originalFilename!!.lowercase().endsWith(".jpeg")
                        && !file.originalFilename!!.lowercase().endsWith(".gif")
                )
    }

    private fun pdfFileNameInvalid(file: MultipartFile): Boolean {
        return file.originalFilename == null || (!file.originalFilename!!.lowercase().endsWith(".pdf"))
    }

    @Transactional(readOnly = true)
    open fun getCategoriesForGroupInRange(groupId: Int, now: Long, advertisedOnly: Boolean = false, userRole: RoleType): List<TaskCategoryDto> {

        val submissionSummaries = submitted.findSubmissionSummaryByGroupId(groupId)
        val submissionByCategory = submissionSummaries.associateBy({ it.categoryId }, { it })

        val taskCountList = taskRepository.findTaskCountByCategory(now)
        val taskCountByCategory = taskCountList.associate { it.categoryId to it.count.toInt() }

        val allCategories = if (advertisedOnly) {
            categories.findAllByAdvertisedTrueAndAvailableFromLessThanAndAvailableToGreaterThan(now, now)
        } else {
            categories.findAllByAvailableFromLessThanAndAvailableToGreaterThan(now, now)
        }

        return allCategories
            .filter { userRole.isAdmin || (userRole.value >= it.minRole.value && userRole.value <= it.maxRole.value) }
            .map { category ->
                val submissionSummary = submissionByCategory[category.categoryId]
                val taskCount = taskCountByCategory[category.categoryId] ?: 0

                TaskCategoryDto(
                    name = category.name,
                    categoryId = category.categoryId,
                    availableFrom = category.availableFrom,
                    availableTo = category.availableTo,
                    type = category.type,

                    sum = taskCount,
                    approved = submissionSummary?.approved?.toInt() ?: 0,
                    rejected = submissionSummary?.rejected?.toInt() ?: 0,
                    notGraded = submissionSummary?.notGraded?.toInt() ?: 0
                )
            }
            .sortedBy { it.categoryId }
    }

    @Transactional(readOnly = true)
    open fun getCategoriesForUserInTimeRange(userId: Int, now: Long, userRole: RoleType): List<TaskCategoryDto> {
        val submissionSummaries = submitted.findSubmissionSummaryByUserId(userId)
        val submissionByCategory = submissionSummaries.associateBy({ it.categoryId }, { it })

        val taskCountList = taskRepository.findTaskCountByCategory(now)
        val taskCountByCategory = taskCountList.associate { it.categoryId to it.count.toInt() }

        val allCategories = categories.findAllByAvailableFromLessThanAndAvailableToGreaterThan(now, now)

        return allCategories
            .filter { userRole.isAdmin || (userRole.value >= it.minRole.value && userRole.value <= it.maxRole.value) }
            .map { category ->
                val submissionSummary = submissionByCategory[category.categoryId]
                val taskCount = taskCountByCategory[category.categoryId] ?: 0

                TaskCategoryDto(
                    name = category.name,
                    categoryId = category.categoryId,
                    availableFrom = category.availableFrom,
                    availableTo = category.availableTo,
                    type = category.type,

                    sum = taskCount,
                    approved = submissionSummary?.approved?.toInt() ?: 0,
                    rejected = submissionSummary?.rejected?.toInt() ?: 0,
                    notGraded = submissionSummary?.notGraded?.toInt() ?: 0
                )
            }
            .sortedBy { it.categoryId }
    }

    @Transactional(readOnly = true)
    open fun getCategoryName(categoryId: Int): String {
        return categories.findAllByCategoryId(categoryId).map { it.name }.firstOrNull() ?: "Nincs ilyen"
    }

    @Transactional(readOnly = true)
    open fun getCategoryAvailableFrom(categoryId: Int): Long {
        return categories.findAllByCategoryId(categoryId).map { it.availableFrom }.firstOrNull() ?: 0
    }

    @Transactional(readOnly = true)
    open fun getCategory(categoryId: Int): TaskCategoryEntity? {
        return categories.findAllByCategoryId(categoryId).firstOrNull()
    }

    @Transactional(readOnly = true)
    open fun getAllSubmissions(groupId: Int): List<SubmittedTaskEntity> {
        return submitted.findAllByGroupId(groupId)
    }

    @Transactional(readOnly = true)
    open fun getAllCategories(): List<TaskCategoryEntity> {
        return categories.findAll()
            .sortedBy { it.categoryId }
    }

    @Transactional(readOnly = true)
    open fun getTotalTasksForUser(user: UserEntity): Int {
        return taskRepository.countAllByVisibleTrue()
    }

    @Transactional(readOnly = true)
    open fun getSubmittedTasksForUser(user: CmschUser): Int {
        return submitted.countAllByUserIdAndRejectedFalseAndApprovedFalse(user.id)
    }

    @Transactional(readOnly = true)
    open fun getCompletedTasksForUser(user: CmschUser): Int {
        return submitted.countAllByUserIdAndRejectedFalseAndApprovedTrue(user.id)
    }

    @Transactional(readOnly = true)
    open fun getTasksThatNeedsToBeCompleted(user: UserEntity): List<String> {
        return categories.findAllByType(TaskCategoryType.PROFILE_REQUIRED)
            .flatMap { taskRepository.findAllByCategoryIdAndVisibleTrue(it.categoryId) }
            .filter { task -> submitted.findByTask_IdAndUserId(task.id, user.id)
                .map { !it.approved }
                .orElse(true)
            }
            .map { it.title }
    }

}
