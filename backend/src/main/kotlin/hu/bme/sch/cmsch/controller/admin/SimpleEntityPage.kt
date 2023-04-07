package hu.bme.sch.cmsch.controller.admin

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.component.ComponentBase
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.model.IdentifiableEntity
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import java.util.function.Supplier
import kotlin.reflect.KClass

abstract class SimpleEntityPage<T : IdentifiableEntity>(
    view: String,
    classType: KClass<T>,
    supplier: Supplier<T>,
    titleSingular: String,
    titlePlural: String,
    description: String,

    private val contentProvider: ((user: CmschUser) -> Iterable<T>),
    permission: PermissionValidator,

    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: ComponentBase,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,

    adminMenuCategory: String? = null,
    adminMenuIcon: String = "check_box_outline_blank",
    adminMenuPriority: Int = 1,

    controlActions: MutableList<ControlAction> = mutableListOf(),
    buttonActions: MutableList<ButtonAction> = mutableListOf()
) : OneDeepEntityPage<T>(
    view,
    classType,
    supplier,
    titleSingular,
    titlePlural,
    description,

    object : ManualRepository<T, Int>() {},
    importService,
    adminMenuService,
    component,
    auditLog,
    objectMapper,

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

    adminMenuCategory = adminMenuCategory,
    adminMenuIcon = adminMenuIcon,
    adminMenuPriority = adminMenuPriority,

    controlActions = controlActions,
    buttonActions = buttonActions
) {

    override fun fetchOverview(user: CmschUser): Iterable<T> {
        return contentProvider.invoke(user)
    }

}