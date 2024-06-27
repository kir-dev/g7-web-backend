package hu.bme.sch.cmsch.component.countdown

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.ApplicationComponent
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_COUNTDOWN
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/countdown")
@ConditionalOnBean(CountdownComponent::class)
class CountdownComponentController(
    adminMenuService: AdminMenuService,
    component: CountdownComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    CountdownComponent::class.java,
    component,
    PERMISSION_CONTROL_COUNTDOWN,
    componentMenuName = "Visszaszámlálás",
    menuService = menuService,
    componentMenuIcon = "alarm",
    insertComponentCategory = false,
    componentCategory = ApplicationComponent.FUNCTIONALITIES_CATEGORY,
    componentMenuPriority = 20,
    auditLogService = auditLogService,
    storageService = storageService
)
