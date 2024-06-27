package hu.bme.sch.cmsch.component.task

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/task")
@ConditionalOnBean(TaskComponent::class)
class TaskController(
    private val repo: TaskEntityRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: TaskComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<TaskEntity>(
    "task",
    TaskEntity::class, ::TaskEntity,
    "Feladat", "Feladatok",
    "Feladatok kezelése. A feladatok javítására használd a \"Feladatok értékelése\" menüt!",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
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

    searchSettings = calculateSearchSettings<TaskEntity>(false)
) {

    override fun fetchOverview(user: CmschUser): Iterable<TaskEntity> {
        return repo.findAllWithoutLobs()
    }

}
