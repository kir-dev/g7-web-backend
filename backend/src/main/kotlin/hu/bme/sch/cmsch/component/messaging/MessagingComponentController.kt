package hu.bme.sch.cmsch.component.messaging

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.sssl.alexandria.gtb.GtbControlPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/messaging")
@ConditionalOnBean(MessagingComponent::class)
class MessagingComponentController(
    adminMenuService: AdminMenuService,
    component: MessagingComponent,
    menuService: MenuService,
    auditLogService: AuditLogService
) : ComponentApiBase(
    adminMenuService,
    MessagingComponent::class.java,
    component,
    GtbControlPermissions.PERMISSION_CONTROL_GTB_STATS,
    "Értesítések",
    "Értesítések beállítása",
    menuService = menuService,
    auditLogService = auditLogService
)
