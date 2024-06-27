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
@RequestMapping("/admin/control/component/footer")
@ConditionalOnBean(ApplicationComponent::class)
class FooterComponentController(
    adminMenuService: AdminMenuService,
    component: FooterComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    FooterComponent::class.java,
    component,
    ControlPermissions.PERMISSION_CONTROL_FOOTER,
    componentMenuName = "Lábléc",
    componentMenuIcon = "footprint",
    menuService = menuService,
    insertComponentCategory = false,
    componentCategory = ApplicationComponent.STYLING_CATEGORY,
    auditLogService = auditLogService,
    storageService = storageService
)
