package hu.bme.sch.cmsch.component.impressum

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.ApplicationComponent
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_IMPRESSUM
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/impressum")
@ConditionalOnBean(ImpressumComponent::class)
class ImpressumComponentController(
    adminMenuService: AdminMenuService,
    component: ImpressumComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    ImpressumComponent::class.java,
    component,
    PERMISSION_CONTROL_IMPRESSUM,
    componentMenuName = "Impresszum",
    menuService = menuService,
    componentMenuIcon = "alternate_email",
    insertComponentCategory = false,
    componentCategory = ApplicationComponent.CONTENT_CATEGORY,
    componentMenuPriority = 21,
    auditLogService = auditLogService,
    storageService = storageService
)
