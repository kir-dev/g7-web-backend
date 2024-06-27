package hu.bme.sch.cmsch.component.token

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.repository.UserRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.transaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/token-properties-team")
@ConditionalOnBean(TokenComponent::class)
@ConditionalOnProperty(
    prefix = "hu.bme.sch.cmsch.startup",
    name = ["token-ownership-mode"],
    havingValue = "USER",
    matchIfMissing = false
)
class TokenAdminTokensByTeamController(
    private val repo: TokenPropertyRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: TokenComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    private val userRepository: UserRepository,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<UserGroupTokenCount>(
    "token-properties-team",
    UserGroupTokenCount::class, ::UserGroupTokenCount,
    "Csoportos tokenek", "Csoportos tokenek",
    "Felhasználói tokenek csoportonként",

    transactionManager,
    object : ManualRepository<UserGroupTokenCount, Int>() {
        override fun findAll(): Iterable<UserGroupTokenCount> {
            transactionManager.transaction(readOnly = true) {
                val data = repo.countByAllUserGroup()
                data.forEach { it.memberCount = userRepository.findAllByGroupName(it.groupName).size }
                val highestTeamMemberCount = data.maxByOrNull { it.correctedPoints }?.memberCount ?: 0
                data.forEach { it.finalPoints = (it.correctedPoints * highestTeamMemberCount).toInt() }
                return data
            }
        }
    },

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission = StaffPermissions.PERMISSION_SHOW_TOKEN_SUBMISSIONS,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission = ImplicitPermissions.PERMISSION_NOBODY,
    deletePermission = ImplicitPermissions.PERMISSION_NOBODY,

    createEnabled = false,
    editEnabled = false,
    deleteEnabled = false,
    importEnabled = false,
    exportEnabled = false,

    adminMenuIcon = "local_activity",
    adminMenuPriority = 4,
)
