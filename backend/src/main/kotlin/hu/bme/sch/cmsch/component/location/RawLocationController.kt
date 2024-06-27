package hu.bme.sch.cmsch.component.location

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.ButtonAction
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.getUser
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/locations")
@ConditionalOnBean(LocationComponent::class)
class RawLocationController(
    val locationService: LocationService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: LocationComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<LocationEntity>(
    "locations",
    LocationEntity::class, ::LocationEntity,
    "Pozíció", "Pozíciók",
    "A helymeghatározás adatai nyersen",

    transactionManager,
    locationService,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_LOCATIONS,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission =   StaffPermissions.PERMISSION_EDIT_LOCATIONS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_LOCATIONS,

    createEnabled = false,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = false,
    exportEnabled = true,

    adminMenuIcon = "place",
    adminMenuPriority = 2,

    buttonActions = mutableListOf(
        ButtonAction(
            "Frissítés",
            "refresh",
            StaffPermissions.PERMISSION_SHOW_LOCATIONS,
            150,
            "refresh",
            false
        )
    ),

    searchSettings = calculateSearchSettings<LocationEntity>(false)
) {

    @GetMapping("/refresh")
    fun refresh(auth: Authentication): String {
        try {
            if (showPermission.validate(auth.getUser()))
                locationService.refresh()
            return "redirect:/admin/control/$view"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "redirect:/admin/control/$view"
    }

}
