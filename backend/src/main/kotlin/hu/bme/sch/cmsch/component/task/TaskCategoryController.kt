package hu.bme.sch.cmsch.component.task

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/task-categories")
@ConditionalOnBean(TaskComponent::class)
class TaskCategoryController(
    repo: TaskCategoryRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: TaskComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<TaskCategoryEntity>(
    "task-categories",
    TaskCategoryEntity::class, ::TaskCategoryEntity,
    "Feladat Kategória", "Feladat kategóriák",
    "Feladatok kategóriájának kezelése. A feladatok javítására használd a \"Feladatok értékelése\" menüt!",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_TASK_CATEGORIES,
    createPermission = StaffPermissions.PERMISSION_CREATE_TASK_CATEGORIES,
    editPermission =   StaffPermissions.PERMISSION_EDIT_TASK_CATEGORIES,
    deletePermission = StaffPermissions.PERMISSION_DELETE_TASK_CATEGORIES,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "category",
    adminMenuPriority = 2,

    searchSettings = calculateSearchSettings<TaskCategoryEntity>(false)
)
