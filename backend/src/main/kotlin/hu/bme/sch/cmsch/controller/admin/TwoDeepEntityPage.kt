package hu.bme.sch.cmsch.controller.admin

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.admin.OverviewBuilder
import hu.bme.sch.cmsch.component.ComponentBase
import hu.bme.sch.cmsch.model.IdentifiableEntity
import hu.bme.sch.cmsch.repository.EntityPageDataSource
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.getUser
import hu.bme.sch.cmsch.util.transaction
import jakarta.annotation.PostConstruct
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.function.Supplier
import kotlin.reflect.KClass

abstract class TwoDeepEntityPage<OUTER : IdentifiableEntity, INNER: IdentifiableEntity>(
    view : String,
    outerClassType: KClass<OUTER>,
    innerClassType: KClass<INNER>,
    innerSupplier: Supplier<INNER>,
    titleSingular: String,
    titlePlural: String,
    description: String,

    transactionManager: PlatformTransactionManager,
    private val outerDataSource: EntityPageDataSource<OUTER, Int>,
    innerDataSource: EntityPageDataSource<INNER, Int>,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    storageService: StorageService,
    component: ComponentBase,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    env: Environment,
    entitySourceMapping: Map<String, (INNER?) -> List<String>> =
        mapOf(Nothing::class.simpleName!! to { listOf() }),

    showPermission: PermissionValidator,
    createPermission: PermissionValidator,
    editPermission: PermissionValidator,
    deletePermission: PermissionValidator,
    internal val viewPermission: PermissionValidator = showPermission,

    showEnabled: Boolean = true,
    createEnabled: Boolean = false,
    editEnabled: Boolean = false,
    deleteEnabled: Boolean = false,
    importEnabled: Boolean = true,
    exportEnabled: Boolean = true,
    duplicateEnabled: Boolean = createEnabled,
    private val viewEnabled: Boolean = true,

    adminMenuCategory: String? = null,
    adminMenuIcon: String = "check_box_outline_blank",
    adminMenuPriority: Int = 1,
    ignoreFromMenu: Boolean = false,

    private val outerControlActions: MutableList<ControlAction> = mutableListOf(),
    innerControlActions: MutableList<ControlAction> = mutableListOf(),
    buttonActions: MutableList<ButtonAction> = mutableListOf()
): OneDeepEntityPage<INNER>(
    view,
    innerClassType,
    innerSupplier,
    titleSingular,
    titlePlural,
    description,

    transactionManager,
    innerDataSource,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,
    entitySourceMapping,

    showPermission,
    createPermission,
    editPermission,
    deletePermission,

    showEnabled,
    createEnabled,
    editEnabled,
    deleteEnabled,
    importEnabled,
    exportEnabled,
    duplicateEnabled,

    adminMenuCategory,
    adminMenuIcon,
    adminMenuPriority,
    ignoreFromMenu,

    innerControlActions,
    buttonActions
) {

    private val outerDescriptor = OverviewBuilder(outerClassType)

    @PostConstruct
    fun initOuter() {
        if (viewEnabled) {
            outerControlActions.add(
                ControlAction(
                    "Megnyitás",
                    "view/{id}",
                    "double_arrow",
                    showPermission,
                    100,
                    usageString = "Kategória megnyitása",
                    basic = true
                )
            )
        }
    }

    @GetMapping("")
    override fun view(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (showPermission.validate(user).not()) {
            model.addAttribute("permission", showPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}", showPermission.permissionString)
            return "admin403"
        }

        model.addAttribute("title", titlePlural)
        model.addAttribute("titleSingular", titleSingular)
        model.addAttribute("description", description)
        model.addAttribute("view", view)

        model.addAttribute("columnData", outerDescriptor.getColumns())
        val overview = transactionManager.transaction(readOnly = true) { fetchOuterOverview() }
        model.addAttribute("tableData", outerDescriptor.getTableData(overview))

        model.addAttribute("user", user)
        model.addAttribute("controlActions", outerControlActions.filter { it.permission.validate(user) })
        model.addAttribute("allControlActions", outerControlActions)
        model.addAttribute("buttonActions", buttonActions.filter { it.permission.validate(user) })

        attachPermissionInfo(model)
        model.addAttribute("permissionShow",
            if (viewEnabled && viewPermission != ImplicitPermissions.PERMISSION_NOBODY)
                viewPermission.permissionString
            else null)
        model.addAttribute("permissionDelete", null)

        return "overview4"
    }

    @GetMapping("/view/{id}")
    fun view(model: Model, auth: Authentication, @PathVariable id: Int): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (viewPermission.validate(user).not()) {
            model.addAttribute("permission", viewPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/view/$id", viewPermission.permissionString)
            return "admin403"
        }

        model.addAttribute("title", titlePlural)
        model.addAttribute("titleSingular", titleSingular)
        model.addAttribute("description", description)
        model.addAttribute("view", view)

        model.addAttribute("columnData", descriptor.getColumns())
        val overview = transactionManager.transaction(readOnly = true) { filterOverview(user, fetchSublist(id)) }
        model.addAttribute("tableData", descriptor.getTableData(overview))

        model.addAttribute("user", user)
        model.addAttribute("controlActions", controlActions.filter { it.permission.validate(user) })
        model.addAttribute("allControlActions", controlActions)
        model.addAttribute("buttonActions", buttonActions.filter { it.permission.validate(user) })

        attachPermissionInfo(model)

        return "overview4"
    }

    open fun fetchOuterOverview(): Iterable<OUTER> {
        return outerDataSource.findAll()
    }

    abstract fun fetchSublist(id: Int): Iterable<INNER>

}
