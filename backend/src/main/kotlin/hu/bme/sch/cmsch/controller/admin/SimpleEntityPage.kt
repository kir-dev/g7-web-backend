package hu.bme.sch.cmsch.controller.admin

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.component.ComponentBase
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.model.IdentifiableEntity
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import org.springframework.core.env.Environment
import org.springframework.transaction.PlatformTransactionManager
import java.util.function.Supplier
import kotlin.reflect.KClass

abstract class SimpleEntityPage<T : IdentifiableEntity>(
    view: String,
    classType: KClass<T>,
    supplier: Supplier<T>,
    titleSingular: String,
    titlePlural: String,
    description: String,

    transactionManager: PlatformTransactionManager,
    private val contentProvider: ((user: CmschUser) -> Iterable<T>),
    permission: PermissionValidator,

    importService: ImportService,
    adminMenuService: AdminMenuService,
    storageService: StorageService,
    component: ComponentBase,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    env: Environment,

    adminMenuCategory: String? = null,
    adminMenuIcon: String = "check_box_outline_blank",
    adminMenuPriority: Int = 1,
    ignoreFromMenu: Boolean = false,

    controlActions: MutableList<ControlAction> = mutableListOf(),
    buttonActions: MutableList<ButtonAction> = mutableListOf(),
    searchSettings: SearchSettings? = null,
) : OneDeepEntityPage<T>(
    view,
    classType,
    supplier,
    titleSingular,
    titlePlural,
    description,

    transactionManager,
    object : ManualRepository<T, Int>() {},
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission = permission,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission = ImplicitPermissions.PERMISSION_NOBODY,
    deletePermission = ImplicitPermissions.PERMISSION_NOBODY,

    showEnabled = false,
    createEnabled = false,
    editEnabled = false,
    deleteEnabled = false,
    importEnabled = false,
    exportEnabled = false,
    duplicateEnabled = false,

    adminMenuCategory = adminMenuCategory,
    adminMenuIcon = adminMenuIcon,
    adminMenuPriority = adminMenuPriority,
    ignoreFromMenu = ignoreFromMenu,

    controlActions = controlActions,
    buttonActions = buttonActions,
    searchSettings = searchSettings,
) {

    override fun fetchOverview(user: CmschUser): Iterable<T> {
        return contentProvider.invoke(user)
    }

}
