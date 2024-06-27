package hu.bme.sch.cmsch.component.home

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.ApplicationComponent
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/home")
@ConditionalOnBean(HomeComponent::class)
class HomeComponentController(
    adminMenuService: AdminMenuService,
    component: HomeComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    HomeComponent::class.java,
    component,
    ControlPermissions.PERMISSION_CONTROL_HOME,
    insertComponentCategory = false,
    componentCategory = ApplicationComponent.CONTENT_CATEGORY,
    componentMenuName = "Kezdőlap",
    componentMenuPriority = 5,
    componentMenuIcon = "home",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
