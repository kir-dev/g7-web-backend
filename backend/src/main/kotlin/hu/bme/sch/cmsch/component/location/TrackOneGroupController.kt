package hu.bme.sch.cmsch.component.location

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.SimpleEntityPage
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_TRACK_ONE_GROUP
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/track-group")
@ConditionalOnBean(LocationComponent::class)
class TrackOneGroupController(
    private val locationService: LocationService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: LocationComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : SimpleEntityPage<TrackGroupVirtualEntity>(
    "track-group",
    TrackGroupVirtualEntity::class, ::TrackGroupVirtualEntity,
    "Csoport követése", "Csoport követése",
    "Csoport követése a térképen",

    transactionManager,
    { locationService.getRecentLocations()
        .map { TrackGroupVirtualEntity(it.id, it.groupName) }.distinct() },

    permission = StaffPermissions.PERMISSION_TRACK_ONE_GROUP,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "my_location",
    adminMenuPriority = 4,

    controlActions = mutableListOf(
        ControlAction(
            "Térkép",
            "tracking/{id}",
            "map",
            PERMISSION_TRACK_ONE_GROUP,
            10,
            true,
            "Térkép megnyitása"
        )
    )
) {

    @GetMapping("/tracking/{id}")
    fun redirectToTracker(@PathVariable id: String): String {
        return "redirect:/admin/control/tracking/${id}"
    }

}
