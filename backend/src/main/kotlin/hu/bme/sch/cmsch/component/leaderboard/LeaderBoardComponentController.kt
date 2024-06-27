package hu.bme.sch.cmsch.component.leaderboard

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
@RequestMapping("/admin/control/component/leaderboard")
@ConditionalOnBean(LeaderBoardComponent::class)
class LeaderBoardComponentController(
    adminMenuService: AdminMenuService,
    component: LeaderBoardComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    LeaderBoardComponent::class.java,
    component,
    ControlPermissions.PERMISSION_CONTROL_LEADERBOARD,
    componentCategoryName = "Toplista",
    componentMenuName = "Toplista testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
