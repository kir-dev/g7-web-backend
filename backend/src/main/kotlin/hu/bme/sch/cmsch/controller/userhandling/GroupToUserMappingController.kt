package hu.bme.sch.cmsch.controller.userhandling

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.component.app.ApplicationComponent
import hu.bme.sch.cmsch.component.app.UserHandlingComponent
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.model.GroupToUserMappingEntity
import hu.bme.sch.cmsch.repository.GroupRepository
import hu.bme.sch.cmsch.repository.GroupToUserMappingRepository
import hu.bme.sch.cmsch.service.*
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/group-to-user")
class GroupToUserMappingController(
    repo: GroupToUserMappingRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: UserHandlingComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    private val groups: GroupRepository,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<GroupToUserMappingEntity>(
    "group-to-user",
    GroupToUserMappingEntity::class, ::GroupToUserMappingEntity,
    "Csoport Tagság", "Csoport tagságok",
    "Felhasználók neptun kód alapján csoportba és szakra rendelése. A hozzárendelés minden bejelentkezésnél " +
            "megtörténik ha van egyezés és még nincs beállítva.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    entitySourceMapping = mapOf("GroupEntity" to { groups.findAll().map { it.name }.toList() }),

    showPermission =   StaffPermissions.PERMISSION_SHOW_GROUP_MAPPINGS,
    createPermission = StaffPermissions.PERMISSION_CREATE_GROUP_MAPPINGS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_GROUP_MAPPINGS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_GROUP_MAPPINGS,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "people",
    adminMenuPriority = 3,
    adminMenuCategory = ApplicationComponent.DATA_SOURCE_CATEGORY,

    searchSettings = calculateSearchSettings<GroupToUserMappingEntity>(false)
)
