package hu.bme.sch.cmsch.component.app

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
@RequestMapping("/admin/control/extra-menus")
@ConditionalOnBean(ApplicationComponent::class)
class ExtraMenuController(
    repo: ExtraMenuRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    private val menuService: MenuService,
    component: ApplicationComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<ExtraMenuEntity>(
    "extra-menus",
    ExtraMenuEntity::class, ::ExtraMenuEntity,
    "Extra menü", "Extra menük",
    "Nem komponenshez vagy laphoz tartozó menü. Meg lehet adni külső és belső hivatkozásokat is.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   ControlPermissions.PERMISSION_CONTROL_APP,
    createPermission = ControlPermissions.PERMISSION_CONTROL_APP,
    editPermission =   ControlPermissions.PERMISSION_CONTROL_APP,
    deletePermission = ControlPermissions.PERMISSION_CONTROL_APP,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuCategory = ApplicationComponent.CONTENT_CATEGORY,
    adminMenuIcon = "new_label",
    adminMenuPriority = 4,

    searchSettings = calculateSearchSettings<ExtraMenuEntity>(false)
) {
    override fun onImported() { menuService.regenerateMenuCache() }
    override fun onEntityChanged(entity: ExtraMenuEntity) { menuService.regenerateMenuCache() }
    override fun onEntitiesPurged() { menuService.regenerateMenuCache() }
    override fun onEntityDeleted(entity: ExtraMenuEntity) { menuService.regenerateMenuCache() }
}
