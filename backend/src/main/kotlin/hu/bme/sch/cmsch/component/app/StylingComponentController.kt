package hu.bme.sch.cmsch.component.app

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/style")
@ConditionalOnBean(StylingComponent::class)
class StylingComponentController(
    adminMenuService: AdminMenuService,
    component: StylingComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    StylingComponent::class.java,
    component,
    ControlPermissions.PERMISSION_CONTROL_APP,
    componentMenuName = "Stílus beállítások",
    componentMenuIcon = "style",
    insertComponentCategory = false,
    componentCategory = ApplicationComponent.STYLING_CATEGORY,
    componentMenuPriority = 5,
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
