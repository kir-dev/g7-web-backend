package hu.bme.sch.cmsch.component.team

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/join-requests")
@ConditionalOnBean(TeamComponent::class)
class TeamController(
    repo: TeamJoinRequestRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: TeamComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<TeamJoinRequestEntity>(
    "join-requests",
    TeamJoinRequestEntity::class, ::TeamJoinRequestEntity,
    "Kérelem", "Kérelmek",
    "A csoport csatlakozási kérelmek",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_TEAM_JOINS,
    createPermission = StaffPermissions.PERMISSION_CREATE_TEAM_JOINS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_TEAM_JOINS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_TEAM_JOINS,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "group_add",
    adminMenuPriority = 2,

    searchSettings = calculateSearchSettings<TeamJoinRequestEntity>(false)
)
