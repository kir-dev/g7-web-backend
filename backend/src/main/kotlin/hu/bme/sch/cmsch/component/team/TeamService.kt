package hu.bme.sch.cmsch.component.team

import hu.bme.sch.cmsch.component.form.FormService
import hu.bme.sch.cmsch.component.leaderboard.LeaderBoardService
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.component.qrfight.QrFightService
import hu.bme.sch.cmsch.component.race.DEFAULT_CATEGORY
import hu.bme.sch.cmsch.component.race.RaceService
import hu.bme.sch.cmsch.component.riddle.RiddleReadonlyService
import hu.bme.sch.cmsch.component.task.TasksService
import hu.bme.sch.cmsch.config.OwnershipType
import hu.bme.sch.cmsch.config.StartupPropertyConfig
import hu.bme.sch.cmsch.model.GroupEntity
import hu.bme.sch.cmsch.model.MajorType
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

val TEAM_LEADER = "Csapatkapitány"

@Service
@ConditionalOnBean(TeamComponent::class)
open class TeamService(
    private val teamComponent: TeamComponent,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val teamJoinRequestRepository: TeamJoinRequestRepository,
    private val teamIntroductionRepository: TeamIntroductionRepository,
    private val leaderBoardService: Optional<LeaderBoardService>,
    private val raceService: Optional<RaceService>,
    private val startupPropertyConfig: StartupPropertyConfig,
    private val tasksService: Optional<TasksService>,
    private val formsService: Optional<FormService>,
    private val qrFightService: Optional<QrFightService>,
    private val riddleReadonlyService: Optional<RiddleReadonlyService>,
    private val clock: TimeService,
    private val storageService: StorageService
) {
    companion object {
        val target = "team"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun createTeam(user: UserEntity, name: String): TeamCreationStatus {
        if (user.group?.leaveable == false)
            return TeamCreationStatus.ALREADY_IN_GROUP

        if (!teamComponent.creationEnabled.isValueTrue())
            return TeamCreationStatus.CREATION_DISABLED

        val finalName = name.trim()
        if (finalName.length > 64 || !finalName.matches(Regex(teamComponent.nameRegex.getValue())))
            return TeamCreationStatus.INVALID_NAME

        if (teamComponent.nameBlocklist.getValue().lowercase().split(Regex(", *")).contains(finalName.lowercase())) {
            log.info("Failed to create group with denied name: '{}' by user '{}'", finalName, user.fullName)
            return TeamCreationStatus.USED_NAME
        }

        if (groupRepository.findByNameIgnoreCase(finalName).isPresent) {
            log.info("Failed to create group with denied name: '{}' by user '{}'", finalName, user.fullName)
            return TeamCreationStatus.USED_NAME
        }

        val groupEntity = GroupEntity(
            0, finalName, MajorType.UNKNOWN,
            "${user.fullName}||", "", "", "",
            "", listOf(),
            teamComponent.racesByDefault.isValueTrue(),
            teamComponent.selectableByDefault.isValueTrue(),
            leaveable = false,
            manuallyCreated = true,
            profileTopMessage = ""
        )
        groupRepository.save(groupEntity)

        val introduction = TeamIntroductionEntity(
            creationDate = clock.getTimeInSeconds(),
            group = groupEntity,
            introduction = "$TEAM_LEADER: ${user.fullNameWithAlias}",
            approved = true,
        )
        teamIntroductionRepository.save(introduction)

        return userRepository.findByInternalId(user.internalId).map {
            it.groupName = groupEntity.name
            it.group = groupEntity
            if (teamComponent.grantPrivilegedRole.isValueTrue() && it.role.value < RoleType.PRIVILEGED.value) {
                log.info("Group '{}' #{} successfully created by user '{}' (PRIVILEGED granted)", finalName, groupEntity.id, user.fullName)
                it.role = RoleType.PRIVILEGED
            } else {
                log.info("Group '{}' #{} successfully created by user '{}' (PRIVILEGED not granted)", finalName, groupEntity.id, user.fullName)
            }
            userRepository.save(it)
            TeamCreationStatus.OK
        }.orElseGet {
            log.error("Group '{}' #{} successfully created by user '{}' (but user not found)", finalName, groupEntity.id, user.fullName)
            TeamCreationStatus.INTERNAL_ERROR
        }
    }

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun joinTeam(user: UserEntity, groupId: Int): TeamJoinStatus {
        if (user.group?.leaveable == false)
            return TeamJoinStatus.ALREADY_IN_GROUP

        if (!teamComponent.joinEnabled.isValueTrue())
            return TeamJoinStatus.JOINING_DISABLED

        val group = groupRepository.findById(groupId)
        if (group.isEmpty)
            return TeamJoinStatus.GROUP_NOT_FOUND

        if (!group.get().selectable)
            return TeamJoinStatus.NOT_JOINABLE

        if (teamJoinRequestRepository.existsByUserId(user.id))
            return TeamJoinStatus.ALREADY_SUBMITTED_JOIN_REQUEST

        teamJoinRequestRepository.save(TeamJoinRequestEntity(0, user.fullNameWithAlias, user.id, group.get().name, groupId))
        log.info("Join request recorded for user '{}' to group '{}'", user.fullName, group.get().name)
        return TeamJoinStatus.OK
    }

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun cancelJoin(user: CmschUser): TeamJoinStatus {
        if (!teamComponent.joinEnabled.isValueTrue())
            return TeamJoinStatus.JOINING_DISABLED

        teamJoinRequestRepository.deleteAllByUserId(user.id)
        log.info("Join request cancelled for user '{}'", user.userName)
        return TeamJoinStatus.OK
    }

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun leaveTeam(user: CmschUser): TeamLeaveStatus {
        if (!teamComponent.leaveEnabled.isValueTrue())
            return TeamLeaveStatus.LEAVING_DISABLED

        if (user.role == RoleType.PRIVILEGED)
            return TeamLeaveStatus.LEADERS_CANNOT_LEAVE

        val userEntity = userRepository.findById(user.id).orElseThrow()
        userEntity.groupName = ""
        userEntity.group = null
        userRepository.save(userEntity)
        log.info("User '{}' left their group", userEntity.fullName)
        return TeamLeaveStatus.OK
    }

    @Transactional(readOnly = true)
    open fun listAllTeams(): List<TeamListView> {
        if (!teamComponent.showTeamsAtAll.isValueTrue())
            return listOf()

        var teams = if (teamComponent.showNotRacingTeams.isValueTrue()) {
            if (teamComponent.showNotManualTeams.isValueTrue()) {
                groupRepository.findAllThatExists()
            } else {
                groupRepository.findAllThatManuallyCreated()
            }
        } else {
            if (teamComponent.showNotManualTeams.isValueTrue()) {
                groupRepository.findAllThatRaces()
            } else {
                groupRepository.findAllThatRacesAndManuallyCreated()
            }
        }

        if (teamComponent.sortByName.isValueTrue())
            teams = teams.sortedBy { it.name }

        val introductions = teamIntroductionRepository.findAll()
            .filter { it.approved }
            .groupBy { it.group?.id ?: 0 }
            .map { entries -> entries.key to entries.value.maxBy { it.creationDate } }
            .toMap()
        teams.forEach { team ->
            val introduction = introductions[team.id]
            if (introduction != null) {
                team.introduction = introduction.introduction
                team.logo = introduction.logo
            }
        }

        return teams
    }

    private fun showThisTeam(team: GroupEntity): Boolean {
        if (!teamComponent.showTeamsAtAll.isValueTrue())
            return false
        if (!teamComponent.showNotRacingTeams.isValueTrue() && !team.races)
            return false
        if (!teamComponent.showNotManualTeams.isValueTrue() && team.manuallyCreated != true)
            return false
        return true
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    open fun showTeam(teamId: Int, user: CmschUser?): OptionalTeamView? {
        val team = groupRepository.findById(teamId)
        if (team.isEmpty)
            return null

        return if (showThisTeam(team.orElseThrow())) {
            OptionalTeamView(TeamStatus.PLAYING, mapTeam(team.orElseThrow(), user, user?.groupId == teamId))
        } else {
            OptionalTeamView(TeamStatus.NOT_VISIBLE, null)
        }
    }

    private fun mapTeam(team: GroupEntity, user: CmschUser?, ownTeam: Boolean): TeamView {
        val joinCancellable = user != null && teamJoinRequestRepository.existsByUserIdAndGroupId(user.id, team.id)
        val joinEnabled = user != null
                && teamComponent.joinEnabled.isValueTrue()
                && (getTeam(user.groupId)?.leaveable ?: true)
                && user.role.value < RoleType.PRIVILEGED.value
                && !joinCancellable
        val leaveEnabled = user != null
                && teamComponent.leaveEnabled.isValueTrue()
                && user.groupId == team.id
                && user.role.value < RoleType.PRIVILEGED.value
        val score = leaderBoardService.map { it.getScoreOfGroup(team.name) }.orElse(null)
        val members = if (teamComponent.showTeamMembersPublicly.isValueTrue() || ownTeam) mapMembers(team, user?.id) else null
        val requests = if (user != null && user.role.value >= RoleType.PRIVILEGED.value && ownTeam) mapRequests(team) else null

        val introductions = teamIntroductionRepository.findIntroductionsForGroup(team.id)
        val latestIntro = introductions.firstOrNull { it.approved }
        val latestRejectedIntro = introductions.firstOrNull { it.rejected }

        // if there is a newer introduction that is rejected than the latest accepted, we should display the reason
        val descriptionRejected = (latestRejectedIntro?.creationDate ?: 0) > (latestIntro?.creationDate ?: 0)
        val rejectionReason = if (descriptionRejected) latestRejectedIntro?.rejectionReason else null
        return TeamView(
            id = team.id,
            name = team.name,
            coverUrl = team.coverImageUrl,
            description = latestIntro?.introduction ?: "",
            descriptionRejected = if (ownTeam) descriptionRejected else false,
            descriptionRejectionReason = if (ownTeam) rejectionReason else null,
            logo = latestIntro?.logo ?: "",
            points = score,
            members = members,
            applicants = requests,
            joinEnabled = joinEnabled,
            leaveEnabled = leaveEnabled,
            joinCancellable = joinCancellable,
            ownTeam = ownTeam,
            stats = mapStats(team),
            taskCategories = if (ownTeam && teamComponent.showTasks.isValueTrue() && user != null) mapTasks(team, user) else listOf(),
            forms = if (user != null && ownTeam && teamComponent.showAdvertisedForms.isValueTrue()) mapForms(user) else listOf(),
            leaderNotes = if (ownTeam && ((user?.role?.value ?: RoleType.GUEST.value) >= RoleType.PRIVILEGED.value)) teamComponent.leaderNotes.getValue() else ""
        )
    }

    private fun getTeam(groupId: Int?): GroupEntity? {
        if (groupId == null)
            return null
        return groupRepository.findById(groupId).getOrNull()
    }

    private fun mapTasks(team: GroupEntity, user: CmschUser): List<TaskCategoryPreview> {
        return tasksService.map { tasks ->
            tasks.getCategoriesForGroupInRange(team.id, clock.getTimeInSeconds(), advertisedOnly = true, user.role)
                .map { TaskCategoryPreview(
                    name = it.name,
                    completed = it.approved,
                    outOf = it.sum,
                    navigate = it.categoryId
                ) }
        }.orElse(listOf())
    }

    private fun mapForms(user: CmschUser): List<AdvertisedFormPreview> {
        return formsService.map { forms ->
            forms.getAllAdvertised(user.role)
                .map { form ->
                    AdvertisedFormPreview(
                    name = form.name,
                    filled = if (form.ownerIsGroup) {
                        doesGroupFilled(forms, form.id, user.groupId)
                    } else {
                        forms.doesUserFilled(userId = user.id, formId = form.id)
                    },
                    availableUntil = form.availableUntil,
                    url = form.url
                ) }
        } .orElse(listOf())
    }

    private fun doesGroupFilled(forms: FormService, formId: Int, groupId: Int?): Boolean {
        if (groupId == null)
            return false
        return forms.doesGroupFilled(groupId, formId)
    }

    private fun mapStats(group: GroupEntity): List<TeamStatView> {
        val stats = mutableListOf<TeamStatView>()

        if (teamComponent.membersStatEnabled.isValueTrue()) {
            stats.add(
                TeamStatView(
                    teamComponent.membersStatHeader.getValue(),
                    "${userRepository.countAllByGroup(group)} db",
                    null,
                    null
                )
            )
        }

        if (teamComponent.placeStatEnabled.isValueTrue()) {
            val place = leaderBoardService.map { it.getPlaceOfGroup(group) }.orElse(0)
            if (place > 0)
                stats.add(TeamStatView(teamComponent.placeStatHeader.getValue(), "$place.", null, "/leaderboard"))
        }

        if (teamComponent.scoreStatEnabled.isValueTrue()) {
            val score = leaderBoardService.map { it.getScoreOfGroup(group.name) }.orElse(null)
            if (score != null)
                stats.add(TeamStatView(teamComponent.scoreStatHeader.getValue(), "$score pont", null, "/leaderboard"))
        }

        if (teamComponent.raceStatEnabled.isValueTrue()) {
            val race = raceService
                .map { service ->
                    when (startupPropertyConfig.raceOwnershipMode) {
                        OwnershipType.USER -> service.getBoardForUsers(DEFAULT_CATEGORY, false)
                        OwnershipType.GROUP -> service.getBoardForGroups(DEFAULT_CATEGORY)
                    }
                }
                .map { collection -> collection.firstOrNull { it.groupName == group.name } }
                .orElse(null)
            if (race != null)
                stats.add(
                    TeamStatView(
                        teamComponent.raceStatHeader.getValue(),
                        race.name,
                        "${race.time} mp",
                        "/race"
                    )
                )
        }

        if (teamComponent.qrFightStatEnabled.isValueTrue()) {
            qrFightService.ifPresent { qrs ->
                stats.add(TeamStatView(
                    name = teamComponent.qrTokenStatHeader.getValue(),
                    value1 = "${qrs.getQrCountForGroup(group.id)} db",
                    value2 = null,
                    navigate = "/qr-fight"
                ))
                stats.add(TeamStatView(
                    name = teamComponent.qrTowerStatHeader.getValue(),
                    value1 = "${qrs.getTowerCountForGroup(group.id)} db",
                    value2 = null,
                    navigate = "/qrfight"
                ))
            }
        }

        if (teamComponent.riddleStatEnabled.isValueTrue()) {
            riddleReadonlyService.ifPresent { riddles ->
                val details = riddles.getRiddleDetails(group.id)
                stats.add(TeamStatView(
                    name = teamComponent.riddleStatHeader.getValue(),
                    value1 = "${details.solved} db",
                    value2 = "Ebből átugrott ${details.skipped} db",
                    percentage = if (details.all == 0) 1f else (details.solved / details.all).toFloat(),
                    navigate = "/riddle"
                ))
            }
        }

        return stats
    }

    private fun mapMembers(team: GroupEntity, userId: Int?): List<TeamMemberView> {
        return userRepository.findAllByGroupName(team.name)
            .map { TeamMemberView(it.fullNameWithAlias, it.id, it.role.value >= RoleType.PRIVILEGED.value, it.id == userId) }
    }

    private fun mapRequests(team: GroupEntity): List<TeamMemberView> {
        return teamJoinRequestRepository.findAllByGroupId(team.id)
            .map { TeamMemberView(it.userName, it.userId, isAdmin = false, isYou = false) }
    }

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun acceptJoin(userId: Int, group: GroupEntity?): Boolean {
        if (group == null)
            return false

        if (teamJoinRequestRepository.existsByUserIdAndGroupId(userId, group.id)) {
            val user = userRepository.findById(userId).orElse(null) ?: return false
            addUserToGroup(user, group)
            userRepository.save(user)
            teamJoinRequestRepository.deleteAllByUserId(userId)
            return true
        }
        return false
    }

    fun addUserToGroup(user: UserEntity, group: GroupEntity) {
        user.groupName = group.name
        user.group = group
        if (teamComponent.grantAttendeeRole.isValueTrue() && user.role.value <= RoleType.ATTENDEE.value) {
            log.info("User '{}' accepted for group '{}' (ATTENDEE granted)", user.fullName, group.name)
            user.role = RoleType.ATTENDEE
        } else {
            log.info("User '{}' accepted for group '{}' (ATTENDEE not granted)", user.fullName, group.name)
        }
    }

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun rejectJoin(userId: Int, groupId: Int?, groupName: String): Boolean {
        if (groupId == null)
            return false

        if (teamJoinRequestRepository.existsByUserIdAndGroupId(userId, groupId)) {
            val user = userRepository.findById(userId).orElse(null) ?: return false
            log.info("User '{}' rejected for group '{}'", user.fullName, groupName)
            teamJoinRequestRepository.deleteAllByUserId(userId)
            return true
        }
        return false
    }

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun toggleUserPermissions(userId: Int, groupId: Int?, groupName: String, adminUser: CmschUser): Boolean {
        if (groupId == null)
            return false

        if (userId == adminUser.id) {
            log.info("Failed to switch user permissions '{}' from '{}' by '{}', reason: can't revoke your permissions",
                adminUser.userName, groupName, adminUser.userName)
            return false
        }

        if (!teamComponent.togglePermissionEnabled.isValueTrue()) {
            log.info("Failed to switch user permissions '{}' from '{}' by '{}', reason: disabled by config",
                adminUser.userName, groupName, adminUser.userName)
            return false
        }

        val user = userRepository.findById(userId).orElse(null)
            ?: return false

        if (user.group?.id != groupId) {
            log.info("Failed to switch user permissions '{}' from '{}', reason: groups does not match",
                user.fullName, groupName)
            return false
        }

        return if (user.role.value >= RoleType.STAFF.value) {
            log.info("User '{}' is at least staff, cannot be revoked", user.fullName)
            true
        } else if (user.role.value < RoleType.PRIVILEGED.value) {
            user.role = RoleType.PRIVILEGED
            userRepository.save(user)
            log.info("User '{}' is now group leader at '{}' by '{}'", user.fullName, groupName, adminUser.userName)
            true
        } else {
            user.role = if (teamComponent.grantPrivilegedRole.isValueTrue()) RoleType.ATTENDEE else RoleType.BASIC
            userRepository.save(user)
            log.info("User '{}' is now regular group member at '{}' by '{}'", user.fullName, groupName, adminUser.userName)
            true
        }
    }

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun promoteLeader(userId: Int, groupId: Int?, groupName: String, adminUser: CmschUser): Boolean {
        if (groupId == null)
            return false

        if (userId == adminUser.id) {
            log.info("Failed to promote leadership to user '{}' from '{}' by '{}', reason: can't revoke your permissions",
                adminUser.userName, groupName, adminUser.userName)
            return false
        }

        if (!teamComponent.promoteLeadershipEnabled.isValueTrue()) {
            log.info("Failed to promote leadership to user '{}' from '{}' by '{}', reason: disabled by config",
                adminUser.userName, groupName, adminUser.userName)
            return false
        }

        val user = userRepository.findById(userId).orElse(null)
            ?: return false

        if (user.group?.id != groupId) {
            log.info("Failed to promote leadership to user '{}' from '{}', reason: groups does not match",
                user.fullName, groupName)
            return false
        }

        if (user.role.value < RoleType.STAFF.value)
            user.role = RoleType.PRIVILEGED
        userRepository.save(user)

        val adminUserEntity = userRepository.findById(adminUser.id).orElse(null)
        if (adminUserEntity.role.value < RoleType.STAFF.value)
            adminUserEntity.role = if (teamComponent.grantPrivilegedRole.isValueTrue()) RoleType.ATTENDEE else RoleType.BASIC
        userRepository.save(adminUserEntity)

        val group = groupRepository.findById(groupId).orElseThrow()
        groupRepository.save(group)
        log.info("User '{}' is now group leader at '{}' switched with '{}'", user.fullName, groupName, adminUser.userName)
        return true
    }

    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun kickUser(userId: Int, groupId: Int?, groupName: String, adminUser: CmschUser): Boolean {
        if (groupId == null)
            return false

        if (userId == adminUser.id) {
            log.info("Failed to kick user '{}' from '{}' by '{}', reason: can't kick yourself",
                adminUser.userName, groupName, adminUser.userName)
            return false
        }

        if (!teamComponent.kickEnabled.isValueTrue()) {
            log.info("Failed to kick user '{}' from '{}' by '{}', reason: disabled by config",
                adminUser.userName, groupName, adminUser.userName)
            return false
        }

        val user = userRepository.findById(userId).orElse(null)
            ?: return false

        if (user.group?.id != groupId) {
            log.info("Failed to kick user '{}' from '{}' by '{}', reason: groups does not match",
                user.fullName, groupName, adminUser.userName)
            return false
        }
        if (user.role.value >= RoleType.PRIVILEGED.value) {
            log.info("Failed to kick user '{}' from '{}' by '{}', reason: admins cannot be kicked",
                user.fullName, groupName, adminUser.userName)
            return false
        }

        user.group = null
        user.groupName = ""
        if (user.role.value < RoleType.STAFF.value)
            user.role = RoleType.BASIC
        userRepository.save(user)
        log.info("User '{}' kicked from '{}', reason: by admin '{}'", user.fullName, groupName, adminUser.userName)
        return true
    }
    @Retryable(value = [ SQLException::class ], maxAttempts = 5, backoff = Backoff(delay = 500L, multiplier = 1.5))
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    open fun setDescriptionAndLogo(description: String, logo: MultipartFile?, user: CmschUser): TeamEditStatus {
        val groupId = user.groupId ?: throw IllegalStateException("The user is not member of a group yet")

        if (logo != null && !teamComponent.teamLogoUploadEnabled.isValueTrue()) {
            return TeamEditStatus.ERROR
        }
        if (logo != null && !isImageNameValid(logo)) {
            throw IllegalStateException("Invalid image format")
        }

        val group = groupRepository.findById(groupId).getOrNull() ?: return TeamEditStatus.ERROR

        val introduction = TeamIntroductionEntity(
            creationDate = clock.getTimeInSeconds(),
            group = group,
            introduction = description,
        )
        if (logo != null)
            introduction.logo = storageService.saveObject(target, logo)
                .orElseThrow { throw IllegalStateException("Failed to save the image") }

        teamIntroductionRepository.save(introduction)
        log.info(
            "Description and icon (optionally) updated for group {}, with values: {}, {}",
            group.name,
            logo?.originalFilename,
            description
        )
        return TeamEditStatus.OK
    }

    private fun isImageNameValid(file: MultipartFile): Boolean {
        return file.originalFilename != null && (
                file.originalFilename!!.lowercase().endsWith(".png")
                        || file.originalFilename!!.lowercase().endsWith(".jpg")
                        || file.originalFilename!!.lowercase().endsWith(".jpeg")
                        || file.originalFilename!!.lowercase().endsWith(".gif")
                )
    }
}
