package hu.bme.sch.cmsch.component.task

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.INVALID_ID_ERROR
import hu.bme.sch.cmsch.controller.admin.TwoDeepEntityPage
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.getUser
import hu.bme.sch.cmsch.util.markdownToHtml
import hu.bme.sch.cmsch.util.transaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/control/rate-tasks")
@ConditionalOnBean(TaskComponent::class)
class TaskAdminRateController(
    private val submittedRepository: SubmittedTaskRepository,
    private val taskRepository: TaskEntityRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: TaskComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService,
    private val clock: TimeService
) : TwoDeepEntityPage<GradedTaskGroupDto, SubmittedTaskEntity>(
    "rate-tasks",
    GradedTaskGroupDto::class,
    SubmittedTaskEntity::class, ::SubmittedTaskEntity,
    "Értékelés", "Értékelések",
    "A beadott feladatok értékelése",

    transactionManager,
    object : ManualRepository<GradedTaskGroupDto, Int>() {
        override fun findAll(): Iterable<GradedTaskGroupDto> {
            val aggregatedResults = submittedRepository.findAllAggregated()

            return aggregatedResults.map {
                GradedTaskGroupDto(
                    it.taskId,
                    it.taskTitle,
                    it.approvedCount.toInt(),
                    it.rejectedCount.toInt(),
                    it.notGradedCount.toInt()
                )
            }.sortedByDescending { it.notGraded }.toList()
        }
    },
    submittedRepository,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_RATE_TASKS,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission =   StaffPermissions.PERMISSION_RATE_TASKS,
    deletePermission = StaffPermissions.PERMISSION_RATE_TASKS,

    createEnabled = false,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = false,
    exportEnabled = false,

    adminMenuIcon = "thumbs_up_down",
    adminMenuPriority = 3,

    outerControlActions = mutableListOf(
        ControlAction(
            "Értékel",
            "rate/{id}",
            "thumbs_up_down",
            StaffPermissions.PERMISSION_RATE_TASKS,
            200,
            false,
            "Feladat kategória értékelése"
        )
    )
) {

    private val rateControlActions = mutableListOf(
        ControlAction(
            "Kijavít",
            "grade/{id}",
            "grade",
            StaffPermissions.PERMISSION_RATE_TASKS,
            100,
            false,
            "Feladat beadás javítása"
        )
    )

    override fun fetchSublist(id: Int): Iterable<SubmittedTaskEntity> {
        return submittedRepository.findByTask_Id(id)
    }

    @GetMapping("/rate/{id}")
    fun rate(@PathVariable id: Int, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (viewPermission.validate(user).not()) {
            model.addAttribute("permission", viewPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/rate/$id", viewPermission.permissionString)
            return "admin403"
        }

        model.addAttribute("title", titlePlural)
        model.addAttribute("titleSingular", titleSingular)
        model.addAttribute("view", view)

        model.addAttribute("columnData", descriptor.getColumns())
        val submissions = transactionManager.transaction(readOnly = true) {
            submittedRepository.findByTask_IdAndRejectedIsFalseAndApprovedIsFalseWithoutLobs(id)
        }
        model.addAttribute("tableData", descriptor.getTableData(submissions))

        model.addAttribute("user", user)
        model.addAttribute("controlActions", rateControlActions.filter { it.permission.validate(user) })
        model.addAttribute("allControlActions", rateControlActions)
        model.addAttribute("buttonActions", buttonActions.filter { it.permission.validate(user) })

        val taskTitle = submissions.firstOrNull()?.task?.title
            ?: (transactionManager.transaction(readOnly = true) { taskRepository.findById(id) })
                .orElseThrow()
                .title
        model.addAttribute("description", "Feladat: $taskTitle");

        return "overview4"
    }

    @GetMapping("/grade/{id}")
    fun grade(@PathVariable id: Int, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (editPermission.validate(user).not()) {
            model.addAttribute("permission", editPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/grade/$id", editPermission.permissionString)
            return "admin403"
        }

        model.addAttribute("title", titleSingular)
        model.addAttribute("editMode", true)
        model.addAttribute("duplicateMode", false)
        model.addAttribute("view", view)
        model.addAttribute("id", id)
        model.addAttribute("inputs", descriptor.getInputs())
        model.addAttribute("mappings", entitySourceMapping)
        model.addAttribute("user", user)
        model.addAttribute("gradeMode", true)
        model.addAttribute("readOnly", false)
        model.addAttribute("entityMode", false)

        val entity = transactionManager.transaction(readOnly = true) { submittedRepository.findById(id) }
        if (entity.isEmpty) {
            model.addAttribute("error", INVALID_ID_ERROR)
        } else {
            model.addAttribute("data", entity.orElseThrow())
            model.addAttribute("taskTitle", entity.orElseThrow().task?.title)
            model.addAttribute("taskDescription", entity.orElseThrow().task?.description?.let { markdownToHtml(it) })
            val maxScore = entity.orElseThrow().task?.maxScore ?: 0
            model.addAttribute("comment", "Feladványhoz tartozó max pont: $maxScore")
            model.addAttribute("maxScore", maxScore)
        }
        return "details"
    }

    @PostMapping("/grade/{id}")
    fun grade(@PathVariable id: Int,
             @ModelAttribute(binding = false) dto: SubmittedTaskEntity,
             model: Model,
             auth: Authentication,
             @RequestParam(defaultValue = "false") delete0: Boolean,
             @RequestParam(defaultValue = "false") delete1: Boolean,
    ): String {
        val user = auth.getUser()
        if (editPermission.validate(user).not()) {
            model.addAttribute("permission", editPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "POST /${view}/grade", editPermission.permissionString)
            return "admin403"
        }

        val entity = transactionManager.transaction(readOnly = true) { submittedRepository.findById(id) }
        if (entity.isEmpty) {
            return "redirect:/admin/control/$view/grade/$id"
        }

        val newValues = StringBuilder("grade new value: ")
        updateEntity(descriptor, user, entity.get(), dto, newValues,  delete0, null, delete1, null)
        if (entity.get().approved && entity.get().rejected)
            entity.get().rejected = false
        saveChangeHistory(entity.get(), user.userName)
        entity.get().id = id
        auditLog.edit(user, component.component, newValues.toString())
        transactionManager.transaction(readOnly = false) {
            submittedRepository.save(entity.get())
        }
        return "redirect:/admin/control/$view/rate/${entity.get().task?.id ?: ""}"
    }

    fun saveChangeHistory(entity: SubmittedTaskEntity, userName: String) {
        entity.addSubmissionHistory(
            date = clock.getTimeInSeconds(),
            submitterName = userName,
            adminResponse = true,
            content = entity.response,
            contentUrl = "",
            status = "${entity.score} pont | ${
                if (entity.approved) "elfogadva"
                else if (entity.rejected) "elutasítva"
                else "nem értékelt"}",
            type = "TEXT"
        )
    }

    override fun onEntityPreSave(entity: SubmittedTaskEntity, auth: Authentication): Boolean {
        if (entity.approved && entity.rejected)
            entity.rejected = false
        saveChangeHistory(entity, auth.getUser().userName)
        return true
    }

}
