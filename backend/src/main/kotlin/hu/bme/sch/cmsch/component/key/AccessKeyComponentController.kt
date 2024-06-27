package hu.bme.sch.cmsch.component.key

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
@RequestMapping("/admin/control/component/accessKeys")
@ConditionalOnBean(AccessKeyComponent::class)
class AccessKeyComponentController(
    adminMenuService: AdminMenuService,
    component: AccessKeyComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    AccessKeyComponent::class.java,
    component,
    ControlPermissions.PERMISSION_CONTROL_EVENTS,
    "Hozzáférési kulcsok",
    "Hozzáférések testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
