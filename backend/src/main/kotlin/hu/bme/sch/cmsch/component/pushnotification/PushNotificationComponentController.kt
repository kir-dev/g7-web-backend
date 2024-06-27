package hu.bme.sch.cmsch.component.pushnotification

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/pushnotification")
@ConditionalOnBean(PushNotificationComponent::class)
class PushNotificationComponentController(
    adminMenuService: AdminMenuService,
    component: PushNotificationComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    PushNotificationComponent::class.java,
    component,
    ControlPermissions.PERMISSION_SEND_NOTIFICATIONS,
    "Push Értesítések",
    "Push Értesítések beállítása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
