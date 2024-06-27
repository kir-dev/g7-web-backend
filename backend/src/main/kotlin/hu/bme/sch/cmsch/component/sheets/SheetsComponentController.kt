package hu.bme.sch.cmsch.component.sheets

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_SHEETS
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/sheets")
@ConditionalOnBean(SheetsComponent::class)
class SheetsComponentController(
    adminMenuService: AdminMenuService,
    component: SheetsComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService = adminMenuService,
    componentClass = SheetsComponent::class.java,
    component = component,
    permissionToShow = PERMISSION_CONTROL_SHEETS,
    componentCategoryName = "Sheets (beta)",
    componentMenuName = "Sheets testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
