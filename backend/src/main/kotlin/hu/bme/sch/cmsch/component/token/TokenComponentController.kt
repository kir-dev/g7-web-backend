package hu.bme.sch.cmsch.component.token

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_TOKEN
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/token")
@ConditionalOnBean(TokenComponent::class)
class TokenComponentController(
    adminMenuService: AdminMenuService,
    component: TokenComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    TokenComponent::class.java,
    component,
    PERMISSION_CONTROL_TOKEN,
    "Tokenek",
    "Tokenek testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
