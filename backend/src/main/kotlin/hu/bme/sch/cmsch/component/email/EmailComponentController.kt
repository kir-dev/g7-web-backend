package hu.bme.sch.cmsch.component.email

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_EMAILS
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/email")
@ConditionalOnBean(EmailComponent::class)
class EmailComponentController(
    adminMenuService: AdminMenuService,
    component: EmailComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    EmailComponent::class.java,
    component,
    PERMISSION_CONTROL_EMAILS,
    "Emailek",
    "Emailek testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
