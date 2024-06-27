package hu.bme.sch.cmsch.component.staticpage

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_STATIC_PAGES
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/staticPage")
@ConditionalOnBean(StaticPageComponent::class)
class StaticPageComponentController(
    adminMenuService: AdminMenuService,
    component: StaticPageComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    StaticPageComponent::class.java,
    component,
    PERMISSION_CONTROL_STATIC_PAGES,
    "Statikus Oldalak",
    "Oldalak testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
