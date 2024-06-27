package hu.bme.sch.cmsch.component.form

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_FORM
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/form")
@ConditionalOnBean(FormComponent::class)
class FormComponentController(
    adminMenuService: AdminMenuService,
    component: FormComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    FormComponent::class.java,
    component,
    PERMISSION_CONTROL_FORM,
    "Űrlap",
    "Űrlapok testreszabása",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
