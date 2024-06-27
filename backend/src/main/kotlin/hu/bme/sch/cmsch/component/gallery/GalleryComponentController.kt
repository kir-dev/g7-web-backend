package hu.bme.sch.cmsch.component.gallery

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_GALLERY
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/gallery")
@ConditionalOnBean(GalleryComponent::class)
class GalleryComponentController(
    adminMenuService: AdminMenuService,
    component: GalleryComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    GalleryComponent::class.java,
    component,
    PERMISSION_CONTROL_GALLERY,
    "Galéria",
    "Galéria testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
