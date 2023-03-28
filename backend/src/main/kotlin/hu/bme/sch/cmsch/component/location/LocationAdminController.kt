package hu.bme.sch.cmsch.component.location

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.ControlPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/location")
@ConditionalOnBean(LocationComponent::class)
class LocationAdminController(
    adminMenuService: AdminMenuService,
    component: LocationComponent,
    menuService: MenuService
) : ComponentApiBase(
    adminMenuService,
    LocationComponent::class.java,
    component,
    ControlPermissions.PERMISSION_CONTROL_LOCATION,
    componentCategoryName = "Helymegosztás",
    componentMenuName = "Helymegosztás",
    menuService = menuService
)
