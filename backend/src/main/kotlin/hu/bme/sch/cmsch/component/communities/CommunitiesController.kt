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
@RequestMapping("/admin/control/community")
@ConditionalOnBean(CommunitiesComponent::class)
class CommunitiesController(
    repo: CommunityRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: CommunitiesComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<CommunityEntity>(
    "community",
    CommunityEntity::class, ::CommunityEntity,
    "Kör", "Körök",
    "Körök kezelése",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_COMMUNITIES,
    createPermission = StaffPermissions.PERMISSION_CREATE_COMMUNITIES,
    editPermission =   StaffPermissions.PERMISSION_EDIT_COMMUNITIES,
    deletePermission = StaffPermissions.PERMISSION_DELETE_COMMUNITIES,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "supervised_user_circle",
    adminMenuPriority = 1,

    searchSettings = calculateSearchSettings<CommunityEntity>(false)
)
