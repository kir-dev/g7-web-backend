package hu.bme.sch.cmsch.component.riddle

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_RIDDLE
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/riddle")
@ConditionalOnBean(RiddleComponent::class)
class RiddleComponentController(
    adminMenuService: AdminMenuService,
    component: RiddleComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    RiddleComponent::class.java,
    component,
    PERMISSION_CONTROL_RIDDLE,
    "Riddleök",
    "Riddleök testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
