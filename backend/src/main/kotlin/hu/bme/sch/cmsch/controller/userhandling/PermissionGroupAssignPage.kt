package hu.bme.sch.cmsch.controller.userhandling

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.admin.GenerateOverview
import hu.bme.sch.cmsch.admin.OVERVIEW_TYPE_ID
import hu.bme.sch.cmsch.component.app.UserHandlingComponent
import hu.bme.sch.cmsch.controller.admin.ButtonAction
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.SimpleEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.model.IdentifiableEntity
import hu.bme.sch.cmsch.repository.PermissionGroupRepository
import hu.bme.sch.cmsch.repository.UserRepository
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ImportService
import hu.bme.sch.cmsch.service.StaffPermissions
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_SHOW_PERMISSION_GROUPS
import hu.bme.sch.cmsch.util.getUser
import hu.bme.sch.cmsch.util.transaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

data class PermissionGroupsAssignDto(
    @property:GenerateOverview(renderer = OVERVIEW_TYPE_ID, columnName = "ID", order = -1)
    override var id: Int = 0,

    @property:GenerateOverview(columnName = "Teljes név", order = 1, useForSearch = true)
    var name: String = "",

    @property:GenerateOverview(columnName = "Csoport", order = 2, useForSearch = true)
    var group: String = "",

    @property:GenerateOverview(columnName = "Neptun", order = 3, useForSearch = true)
    var neptun: String = "",

    @property:GenerateOverview(columnName = "Email", order = 4, useForSearch = true)
    var email: String = "",

) : IdentifiableEntity

@Controller
@RequestMapping("/admin/control/permission-groups-assign")
@ConditionalOnBean(UserHandlingComponent::class)
class PermissionGroupAssignPage(
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: UserHandlingComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    env: Environment,
    storageService: StorageService,
    transactionManager: PlatformTransactionManager,
    private val userRepository: UserRepository,
    private val permissionGroupRepository: PermissionGroupRepository,
) : SimpleEntityPage<PermissionGroupsAssignDto>(
    "permission-groups-assign",
    PermissionGroupsAssignDto::class, ::PermissionGroupsAssignDto,
    "Elérhető felhasználók", "Elérhető felhasználók",
    "Válassz, hogy melyik felhasználó kapja meg a(z) XY jogkört",

    transactionManager,
    {
        listOf()
    },

    permission = StaffPermissions.PERMISSION_SHOW_USERS,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "account_box",
    adminMenuPriority = 5,
    ignoreFromMenu = true,

    searchSettings = calculateSearchSettings<PermissionGroupsAssignDto>(false),
) {

    private val addPermission = StaffPermissions.PERMISSION_EDIT_USERS

    @GetMapping("/{id}")
    fun viewPermissionGroup(model: Model, auth: Authentication, @PathVariable id: Int): String {
        val controlActionsForView = mutableListOf(
            ControlAction(
                "Hozzáadás",
                "$id/add/{id}",
                "add",
                addPermission,
                200,
                usageString = "Jogkör hozzárendelése",
                basic = false
            )
        )
        val buttonActionsForView = mutableListOf(
            ButtonAction(
                "Vissza",
                "$id/back",
                PERMISSION_SHOW_PERMISSION_GROUPS,
                100,
                "arrow_back",
                false
            ),
        )

        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (showPermission.validate(user).not()) {
            model.addAttribute("permission", showPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}", showPermission.permissionString)
            return "admin403"
        }

        val permissionGroup = transactionManager.transaction { permissionGroupRepository.findById(id).orElseThrow() }

        model.addAttribute("title", "Elérhető felhasználók")
        model.addAttribute("titleSingular", "Elérhető felhasználók")
        model.addAttribute("description", "Válassz, hogy melyik felhasználó kapja meg a(z) ${permissionGroup.displayName} jogkört")
        model.addAttribute("view", view)

        model.addAttribute("columnData", descriptor.getColumns())
        val overview = transactionManager.transaction(readOnly = true) {
            userRepository.findAll()
                .filter { permissionGroup.key.isNotEmpty() && !it.permissionGroups.split(",").contains(permissionGroup.key) }
                .map {
                    PermissionGroupsAssignDto(
                        id = it.id,
                        name = it.fullName,
                        group = it.groupName,
                        neptun = it.neptun,
                        email = it.email,
                    )
                }
        }
        model.addAttribute("tableData", descriptor.getTableData(filterOverview(user, overview)))

        model.addAttribute("user", user)
        model.addAttribute("controlActions", controlActionsForView.filter { it.permission.validate(user) })
        model.addAttribute("allControlActions", controlActionsForView)
        model.addAttribute("buttonActions", buttonActionsForView.filter { it.permission.validate(user) })
        model.addAttribute("searchSettings", searchSettings)

        attachPermissionInfo(model)

        return "overview4"
    }

    @GetMapping("/{permissionGroupId}/add/{userId}")
    fun removePermissionGroupFromUser(auth: Authentication, model: Model, @PathVariable permissionGroupId: Int, @PathVariable userId: Int): String {
        val user = auth.getUser()
        if (addPermission.validate(user).not()) {
            model.addAttribute("permission", addPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/$permissionGroupId/add/$userId", deletePermission.permissionString)
            return "admin403"
        }

        transactionManager.transaction(readOnly = false, isolation = TransactionDefinition.ISOLATION_REPEATABLE_READ) {
            val userToModify = userRepository.findById(userId).orElseThrow()
            val permissionGroup = permissionGroupRepository.findById(permissionGroupId).orElseThrow()

            val groups = userToModify.permissionGroups.split(",").toMutableList()
            groups.add(permissionGroup.key)
            userToModify.permissionGroups = groups.distinct().joinToString(",")

            userRepository.save(userToModify)
            auditLog.edit(user, component.component, "Added permission group: ${permissionGroup.key} to user ${userToModify.fullName}")
            adminMenuService.invalidateUser(userToModify.internalId)
        }

        return "redirect:/admin/control/$view/${permissionGroupId}"
    }

    @GetMapping("/{id}/back")
    fun redirectToUsersView(@PathVariable id: Int) = "redirect:/admin/control/permission-groups-for-users/${id}"

}
