package hu.bme.sch.cmsch.controller.userhandling

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.component.ComponentBase
import hu.bme.sch.cmsch.component.app.UserHandlingComponent
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.component.staticpage.StaticPageService
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.model.PermissionGroupEntity
import hu.bme.sch.cmsch.repository.PermissionGroupRepository
import hu.bme.sch.cmsch.service.*
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@Controller
@RequestMapping("/admin/control/permission-groups")
class PermissionGroupController(
    repo: PermissionGroupRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: UserHandlingComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    private val staticPageService: Optional<StaticPageService>,
    components: MutableList<out ComponentBase>,
    env: Environment,
    storageService: StorageService,
    transactionManager: PlatformTransactionManager,
    private val permissionsService: PermissionsService,
    private val permissionGroupService: PermissionGroupService,
) : OneDeepEntityPage<PermissionGroupEntity>(
    "permission-groups",
    PermissionGroupEntity::class, ::PermissionGroupEntity,
    "Jogkör", "Jogkörök",
    "Jogosultságok csoportosítva, hogy könnyedén felhasználókhoz rendelhető legyen. " +
            "A felhasználó által használható jogosultságok listája az összes egyéni jogosultságainak és a jögköreiban szereplők halmaza.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission = StaffPermissions.PERMISSION_SHOW_PERMISSION_GROUPS,
    createPermission = StaffPermissions.PERMISSION_CREATE_PERMISSION_GROUPS,
    editPermission = StaffPermissions.PERMISSION_EDIT_PERMISSION_GROUPS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_PERMISSION_GROUPS,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "shield_person",
    adminMenuPriority = 4,

    searchSettings = calculateSearchSettings<PermissionGroupEntity>(fuzzy = false),

    controlActions = mutableListOf(
        ControlAction(
            "Felhasználók",
            "users/{id}",
            "rule",
            StaffPermissions.PERMISSION_SHOW_USERS,
            160,
            false,
            "Jogkörbe tartozó felhasználók"
        )
    )
) {

    private val componentClasses = components.map { it::class }.toSet()

    override fun onDetailsView(entity: CmschUser, model: Model) {
        val customPermissions = staticPageService.map { service ->
            service.getAll().groupBy { it.permissionToEdit }.map { group ->
                PermissionValidator(
                    group.key,
                    "Szükséges a(z) '${group.value.joinToString("', '") { it.title }}' " +
                            "nevű oldal(ak) szerkesztéséhez"
                )
            }
        }.orElse(listOf())

        val staffPermissions = permissionsService.allStaffPermissions
            .filter { it.component != null }
            .filter { it.component in componentClasses }
            .filter { it.permissionString.isNotEmpty() }
        val adminPermissions = permissionsService.allControlPermissions
            .filter { it.component != null }
            .filter { it.component in componentClasses }
            .filter { it.permissionString.isNotEmpty() }
        model.addAttribute("customPermissions", customPermissions)
        model.addAttribute("staffPermissions", staffPermissions)
        model.addAttribute("adminPermissions", adminPermissions)
        model.addAttribute("customPermissionList", customPermissions.map { it.permissionString })
        model.addAttribute("staffPermissionList", staffPermissions.map { it.permissionString })
        model.addAttribute("adminPermissionList", adminPermissions.map { it.permissionString })
    }

    override fun onEntityChanged(entity: PermissionGroupEntity) {
        permissionGroupService.invalidatePermissionCaches()
    }

    @GetMapping("/users/{id}")
    fun redirectToUsersView(@PathVariable id: Int) = "redirect:/admin/control/permission-groups-for-users/${id}"

}
