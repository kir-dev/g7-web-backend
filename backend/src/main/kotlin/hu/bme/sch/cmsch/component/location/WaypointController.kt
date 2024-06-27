package hu.bme.sch.cmsch.component.location

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
@RequestMapping("/admin/control/waypoints")
@ConditionalOnBean(LocationComponent::class)
class WaypointController(
    repo: WaypointRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: LocationComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<WaypointEntity>(
    "waypoints",
    WaypointEntity::class, ::WaypointEntity,
    "Jelző", "Jelzők",
    "Fix pontok a térképen",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_LOCATIONS,
    createPermission = StaffPermissions.PERMISSION_CREATE_LOCATIONS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_LOCATIONS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_LOCATIONS,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "push_pin",
    adminMenuPriority = 6,

    searchSettings = calculateSearchSettings<WaypointEntity>(false)
)
