package hu.bme.sch.cmsch.component.communities

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
@RequestMapping("/admin/control/component/communities")
@ConditionalOnBean(CommunitiesComponent::class)
class CommunitiesComponentController(
    adminMenuService: AdminMenuService,
    component: CommunitiesComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    CommunitiesComponent::class.java,
    component,
    ControlPermissions.PERMISSION_CONTROL_COMMUNITIES,
    componentCategoryName = "Körök",
    componentMenuName = "Beállítások",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
