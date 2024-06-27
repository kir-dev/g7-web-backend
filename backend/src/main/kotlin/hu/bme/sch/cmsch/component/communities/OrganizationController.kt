package hu.bme.sch.cmsch.component.communities

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
@RequestMapping("/admin/control/organization")
@ConditionalOnBean(CommunitiesComponent::class)
class OrganizationController(
    repo: OrganizationRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: CommunitiesComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<OrganizationEntity>(
    "organization",
    OrganizationEntity::class, ::OrganizationEntity,
    "Reszort", "Reszortok",
    "Reszortok kezelése",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_EDIT_COMMUNITIES,
    createPermission = StaffPermissions.PERMISSION_EDIT_COMMUNITIES,
    editPermission =   StaffPermissions.PERMISSION_EDIT_COMMUNITIES,
    deletePermission = StaffPermissions.PERMISSION_EDIT_COMMUNITIES,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "category",
    adminMenuPriority = 2,

    searchSettings = calculateSearchSettings<OrganizationEntity>(false)
)
