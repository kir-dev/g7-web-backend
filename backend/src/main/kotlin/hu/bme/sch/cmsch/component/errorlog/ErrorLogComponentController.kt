package hu.bme.sch.cmsch.component.errorlog

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_ERROR_LOG
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/errorlog")
@ConditionalOnBean(ErrorLogComponent::class)
class ErrorLogComponentController(
    adminMenuService: AdminMenuService,
    component: ErrorLogComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService,
) : ComponentApiBase(
    adminMenuService,
    ErrorLogComponent::class.java,
    component,
    PERMISSION_CONTROL_ERROR_LOG,
    "Kliens hibaüzenetek",
    "Kliens hibaüzenetek testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
