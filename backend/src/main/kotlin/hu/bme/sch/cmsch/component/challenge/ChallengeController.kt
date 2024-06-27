package hu.bme.sch.cmsch.component.challenge

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.config.OwnershipType
import hu.bme.sch.cmsch.config.StartupPropertyConfig
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.model.UserEntity
import hu.bme.sch.cmsch.repository.GroupRepository
import hu.bme.sch.cmsch.repository.UserRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.transaction
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/challenge")
@ConditionalOnBean(ChallengeComponent::class)
class ChallengeController(
    repo: ChallengeSubmissionRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: ChallengeComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    private val groups: GroupRepository,
    private val users: UserRepository,
    private val startupPropertyConfig: StartupPropertyConfig,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<ChallengeSubmissionEntity>(
    "challenge",
    ChallengeSubmissionEntity::class, ::ChallengeSubmissionEntity,
    "Beadás", "Beadások",
    "Olyan feladatok aminek az adminisztrálása manuálisan megy. Például személyesen bemutatható feladatok " +
            "vagy külső adatforrásból származó pontok. Pont korrekciónak is használható.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    entitySourceMapping = mapOf(
        "GroupEntity" to {
            val results = mutableListOf<String>()
            results.add("-")
            results.addAll(transactionManager.transaction(readOnly = true) { groups.findAll() }
                .map { it.name }
                .sorted()
                .toList())
            return@to results
        },
        "UserEntity" to {
            val results = mutableListOf<String>()
            results.add("-")
            results.addAll(transactionManager.transaction(readOnly = true) { users.findAll() }
                .sortedBy { it.fullName }
                .map { mapUsername(it) }
                .toList())
            return@to results
        },
    ),

    showPermission =   StaffPermissions.PERMISSION_SHOW_CHALLENGES,
    createPermission = StaffPermissions.PERMISSION_CREATE_CHALLENGES,
    editPermission =   StaffPermissions.PERMISSION_EDIT_CHALLENGES,
    deletePermission = StaffPermissions.PERMISSION_DELETE_CHALLENGES,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "task_alt",
    adminMenuPriority = 1,

    searchSettings = calculateSearchSettings<ChallengeSubmissionEntity>(false)
) {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun onEntityPreSave(entity: ChallengeSubmissionEntity, auth: Authentication): Boolean {
        return when (startupPropertyConfig.challengeOwnershipMode) {
            OwnershipType.USER -> processUserSubmission(entity)
            OwnershipType.GROUP -> processGroupSubmission(entity)
        }
    }

    private fun processGroupSubmission(entity: ChallengeSubmissionEntity): Boolean {
        if (entity.groupName.isNotBlank()) {
            val groupEntity = transactionManager.transaction(readOnly = true) { groups.findByName(entity.groupName) }
            if (groupEntity.isPresent) {
                entity.groupId = groupEntity.orElseThrow().id
                entity.groupName = groupEntity.orElseThrow().name
                entity.userName = ""
                entity.userId = 0
            } else {
                log.error("Group not found: {} so rejected", entity.userName)
                return false
            }
        }
        return true
    }

    private fun processUserSubmission(entity: ChallengeSubmissionEntity): Boolean {
        if (entity.userName.isNotBlank() && entity.userName != "-") {
            val id = entity.userName.split("|")[0].trim().toIntOrNull() ?: 0
            val user = transactionManager.transaction(readOnly = true) { users.findById(id) }

            if (user.isPresent) {
                entity.userName = user.orElseThrow().fullName
                entity.userId = user.orElseThrow().id
                entity.groupId = user.orElseThrow().group?.id
                entity.groupName = user.orElseThrow().groupName
            } else {
                log.error("User not found: {} so rejected", entity.userName)
                return false
            }
        }
        return true
    }

    override fun onPreEdit(actualEntity: ChallengeSubmissionEntity): ChallengeSubmissionEntity {
        val userId = actualEntity.userId ?: return actualEntity
        val copy = actualEntity.copy()
        val user = transactionManager.transaction(readOnly = true) { users.findById(userId) }.orElse(null) ?: return actualEntity
        copy.userName = mapUsername(user)
        return copy
    }
}

private fun mapUsername(it: UserEntity) =
    "${it.id}| ${it.fullNameWithAlias} [${it.provider.firstOrNull() ?: 'n'}] ${it.email}"
