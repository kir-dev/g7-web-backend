package hu.bme.sch.cmsch.component.admission

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/admission")
@ConditionalOnBean(AdmissionComponent::class)
class AdmissionComponentController(
    adminMenuService: AdminMenuService,
    component: AdmissionComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    AdmissionComponent::class.java,
    component,
    ControlPermissions.PERMISSION_CONTROL_ADMISSION,
    componentCategoryName = "Beléptetés",
    componentMenuName = "Jogosultságok",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
) {
    init {
        adminMenuService.registerEntry(AdmissionComponent::class.java.simpleName, AdminMenuEntry(
            "Beléptetés",
            "mobile_friendly",
            "/admin/admission/",
            1,
            StaffPermissions.PERMISSION_VALIDATE_ADMISSION
        ))
        adminMenuService.registerEntry(AdmissionComponent::class.java.simpleName, AdminMenuEntry(
            "Jegyellenőrzés",
            "mobile_friendly",
            "/admin/admission/ticket",
            6,
            StaffPermissions.PERMISSION_VALIDATE_ADMISSION
        ))
    }
}
