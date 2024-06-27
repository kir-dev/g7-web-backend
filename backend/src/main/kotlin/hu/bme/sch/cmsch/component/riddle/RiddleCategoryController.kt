package hu.bme.sch.cmsch.component.riddle

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
@RequestMapping("/admin/control/riddle-categories")
@ConditionalOnBean(RiddleComponent::class)
class RiddleCategoryController(
    repo: RiddleCategoryRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: RiddleComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService,
    private val riddleCacheManager: RiddleCacheManager
) : OneDeepEntityPage<RiddleCategoryEntity>(
    "riddle-categories",
    RiddleCategoryEntity::class, ::RiddleCategoryEntity,
    "Riddle Kategória", "Riddle Kategóriák",
    "Képrejtvény kategóriák kezelése.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_RIDDLE_CATEGORIES,
    createPermission = StaffPermissions.PERMISSION_CREATE_RIDDLE_CATEGORIES,
    editPermission =   StaffPermissions.PERMISSION_EDIT_RIDDLE_CATEGORIES,
    deletePermission = StaffPermissions.PERMISSION_DELETE_RIDDLE_CATEGORIES,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "category",
    adminMenuPriority = 2,

    searchSettings = calculateSearchSettings<RiddleCategoryEntity>(false)
) {

    override fun onEntityChanged(entity: RiddleCategoryEntity) {
        riddleCacheManager.resetCache(persistMapping = false, overrideMappings = false)
    }

}
