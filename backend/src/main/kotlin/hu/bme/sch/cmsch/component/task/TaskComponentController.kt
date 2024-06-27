package hu.bme.sch.cmsch.component.task

import hu.bme.sch.cmsch.component.ComponentApiBase
import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_TASKS
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/component/task")
@ConditionalOnBean(TaskComponent::class)
class TaskComponentController(
    adminMenuService: AdminMenuService,
    component: TaskComponent,
    menuService: MenuService,
    auditLogService: AuditLogService,
    storageService: StorageService
) : ComponentApiBase(
    adminMenuService,
    TaskComponent::class.java,
    component,
    PERMISSION_CONTROL_TASKS,
    "Feladatok",
    "Feladat beállítások",
    menuService = menuService,
    auditLogService = auditLogService,
    storageService = storageService
)
