package hu.bme.sch.cmsch.component.errorlog

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ImplicitPermissions
import hu.bme.sch.cmsch.service.ImportService
import hu.bme.sch.cmsch.service.StaffPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/errorlog")
@ConditionalOnBean(ErrorLogComponent::class)
class ErrorLogController(
    repo: ErrorLogRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: ErrorLogComponent,
    storageService: StorageService,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment
) : OneDeepEntityPage<ErrorLogEntity>(
    "errorlog",
    ErrorLogEntity::class, ::ErrorLogEntity,
    "Hibaüzenet", "Hibaüzenetek",
    "A jelentett hibaüzenetek megtekintése",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission = StaffPermissions.PERMISSION_SHOW_ERROR_LOG,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission = ImplicitPermissions.PERMISSION_NOBODY,
    deletePermission = StaffPermissions.PERMISSION_DELETE_ERROR_LOG,

    createEnabled = false,
    editEnabled = false,
    deleteEnabled = true,
    importEnabled = false,
    exportEnabled = true,

    adminMenuIcon = "error",
    adminMenuPriority = 1,
    searchSettings = calculateSearchSettings<ErrorLogEntity>(true)
)
