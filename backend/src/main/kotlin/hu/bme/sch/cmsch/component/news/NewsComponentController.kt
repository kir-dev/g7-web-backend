package hu.bme.sch.cmsch.component.news

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_NEWS
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/news")
@ConditionalOnBean(NewsComponent::class)
class NewsComponentController(
    adminMenuService: AdminMenuService,
    component: NewsComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    NewsComponent::class.java,
    component,
    PERMISSION_CONTROL_NEWS,
    "Hírek",
    "Hírek testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
