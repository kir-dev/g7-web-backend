package hu.bme.sch.cmsch.controller.userhandling

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.admin.ImportFormat
import hu.bme.sch.cmsch.admin.OverviewBuilder
import hu.bme.sch.cmsch.component.app.UserHandlingComponent
import hu.bme.sch.cmsch.controller.admin.ButtonAction
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.model.GroupEntity
import hu.bme.sch.cmsch.repository.GroupRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.getUser
import hu.bme.sch.cmsch.util.transaction
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/admin/control/groups")
class GroupController(
    repo: GroupRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: UserHandlingComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<GroupEntity>(
    "groups",
    GroupEntity::class, ::GroupEntity,
    "Csoport", "Csoportok",
    "Az összes csoport kezelése. A csoportba való hozzárendelés a felhasználók menüből érhető el!",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    entitySourceMapping = mapOf("UserEntity" to { group ->
        transactionManager.transaction(readOnly = true) {
            group?.members?.map { member ->
                "${member.fullName} (${member.role.name})"
            }?.toList() ?: listOf("Üres")
        }
    }),

    showPermission =   StaffPermissions.PERMISSION_SHOW_GROUPS,
    createPermission = StaffPermissions.PERMISSION_CREATE_GROUPS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_GROUPS,
    deletePermission = ImplicitPermissions.PERMISSION_SUPERUSER_ONLY,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "groups",
    adminMenuPriority = 2,

    buttonActions = mutableListOf(
        ButtonAction(
            name = "Id-Név export",
            "filtered-export/csv",
            StaffPermissions.PERMISSION_SHOW_GROUPS,
            300,
            icon = "export_notes",
            primary = false,
            newPage = true
        )
    ),

    searchSettings = calculateSearchSettings<GroupEntity>(false)

) {

    data class GroupFilteredExportView(
        @property:ImportFormat
        var groupId: Int = 0,

        @property:ImportFormat
        var groupName: String = ""
    )

    private val filterDescriptor = OverviewBuilder(GroupFilteredExportView::class)

    @ResponseBody
    @GetMapping("/filtered-export/csv", produces = [ MediaType.APPLICATION_OCTET_STREAM_VALUE ])
    fun filteredExport(auth: Authentication, response: HttpServletResponse): ByteArray {
        val user = auth.getUser()
        if (!showPermission.validate(user)) {
            throw IllegalStateException("Insufficient permissions")
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"$view-filtered-export.csv\"")
        return filterDescriptor.exportToCsv(fetchOverview(user).map { GroupFilteredExportView(it.id, it.name) }).toByteArray()
    }

}
