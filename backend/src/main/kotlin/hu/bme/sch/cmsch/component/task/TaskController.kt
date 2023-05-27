package hu.bme.sch.cmsch.component.task

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.ImportService
import hu.bme.sch.cmsch.service.StaffPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/task")
@ConditionalOnBean(TaskComponent::class)
class TaskController(
    repo: TaskEntityRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: TaskComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    env: Environment
) : OneDeepEntityPage<TaskEntity>(
    "task",
    TaskEntity::class, ::TaskEntity,
    "Feladat", "Feladatok",
    "Feladatok kezelése. A feladatok javítására használd a \"Feladatok értékelése\" menüt!",

    repo,
    importService,
    adminMenuService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_TASKS,
    createPermission = StaffPermissions.PERMISSION_CREATE_TASKS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_TASKS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_TASKS,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "task",
    adminMenuPriority = 1,
)
