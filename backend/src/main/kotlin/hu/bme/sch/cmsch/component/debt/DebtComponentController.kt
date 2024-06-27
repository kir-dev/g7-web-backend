package hu.bme.sch.cmsch.component.debt

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_DEBTS
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/debt")
@ConditionalOnBean(DebtComponent::class)
class DebtComponentController(
    adminMenuService: AdminMenuService,
    component: DebtComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    DebtComponent::class.java,
    component,
    PERMISSION_CONTROL_DEBTS,
    "Tartozások",
    "Tartozások testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
