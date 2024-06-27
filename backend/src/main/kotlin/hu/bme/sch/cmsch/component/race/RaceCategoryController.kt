package hu.bme.sch.cmsch.component.race

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
@RequestMapping("/admin/control/race-categories")
@ConditionalOnBean(RaceComponent::class)
class RaceCategoryController(
    repo: RaceCategoryRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: RaceComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<RaceCategoryEntity>(
    "race-categories",
    RaceCategoryEntity::class, ::RaceCategoryEntity,
    "Kategória", "Mérés kategóriák",
    "Időmérés extra kategóriái",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_RACE_CATEGORY,
    createPermission = StaffPermissions.PERMISSION_CREATE_RACE_CATEGORY,
    editPermission =   StaffPermissions.PERMISSION_EDIT_RACE_CATEGORY,
    deletePermission = StaffPermissions.PERMISSION_DELETE_RACE_CATEGORY,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "category",
    adminMenuPriority = 4,

    searchSettings = calculateSearchSettings<RaceCategoryEntity>(false)
)
