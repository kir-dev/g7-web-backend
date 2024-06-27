package hu.bme.sch.cmsch.controller.userhandling

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.component.ComponentBase
import hu.bme.sch.cmsch.component.app.UserHandlingComponent
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.component.staticpage.StaticPageService
import hu.bme.sch.cmsch.config.StartupPropertyConfig
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.model.UserEntity
import hu.bme.sch.cmsch.repository.GroupRepository
import hu.bme.sch.cmsch.repository.UserRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.transaction
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@Controller
@RequestMapping("/admin/control/users")
class UserController(
    repo: UserRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: UserHandlingComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    private val profileService: UserProfileGeneratorService,
    private val groups: GroupRepository,
    private val users: UserRepository,
    private val staticPageService: Optional<StaticPageService>,
    private val startupPropertyConfig: StartupPropertyConfig,
    components: MutableList<out ComponentBase>,
    env: Environment,
    storageService: StorageService,
    transactionManager: PlatformTransactionManager,
    private val permissionsService: PermissionsService,
    private val permissionGroupService: PermissionGroupService
) : OneDeepEntityPage<UserEntity>(
    "users",
    UserEntity::class, ::UserEntity,
    "Felhasználó", "Felhasználók",
    "Az összes felhasználó (résztvevők és rendezők egyaránt) kezelése.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    entitySourceMapping = mapOf("GroupEntity" to { listOf("") + groups.findAll().map { it.name }.toList() }),

    showPermission = StaffPermissions.PERMISSION_SHOW_USERS,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission = StaffPermissions.PERMISSION_EDIT_USERS,
    deletePermission = ImplicitPermissions.PERMISSION_SUPERUSER_ONLY,

    createEnabled = false,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = false,
    exportEnabled = true,

    adminMenuIcon = "person",
    adminMenuPriority = 1,

    searchSettings = calculateSearchSettings<UserEntity>(fuzzy = false)
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
        val permissionGroups = permissionGroupService.allPermissionGroups
        model.addAttribute("customPermissions", customPermissions)
        model.addAttribute("staffPermissions", staffPermissions)
        model.addAttribute("adminPermissions", adminPermissions)
        model.addAttribute("permissionGroups", permissionGroups)
        model.addAttribute("customPermissionList", customPermissions.map { it.permissionString })
        model.addAttribute("staffPermissionList", staffPermissions.map { it.permissionString })
        model.addAttribute("adminPermissionList", adminPermissions.map { it.permissionString })
        model.addAttribute("permissionGroupList", permissionGroups.map { it.key })
    }

    override fun onEntityPreSave(entity: UserEntity, auth: Authentication): Boolean {
        if (startupPropertyConfig.profileQrEnabled) {
            profileService.generateFullProfileForUser(entity)
        } else {
            profileService.generateProfileIdForUser(entity)
        }

        if (entity.groupName.isNotBlank()) {
            transactionManager.transaction(readOnly = true) { groups.findByName(entity.groupName) }.ifPresentOrElse({
                entity.group = it
            }, {
                entity.fullName = ""
                entity.group = null
            })
        } else {
            entity.group = null
        }

        adminMenuService.invalidateUser(entity.internalId)
        return true
    }

    override fun fetchOverview(user: CmschUser): Iterable<UserEntity> {
        return users.findAllUserHandlerView()
            .map {
                UserEntity(
                    id = it.id,
                    fullName = it.name,
                    alias = it.alias,
                    neptun = it.neptun,
                    groupName = it.groupName,
                    email = it.email,
                    guild = it.guild
                )
            }
    }

}
