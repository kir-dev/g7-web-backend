package hu.bme.sch.cmsch.component.bmejegy

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
@RequestMapping("/admin/control/bmejegy-tickets")
@ConditionalOnBean(BmejegyComponent::class)
class BmejegyController(
    repo: BmejegyRecordRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: BmejegyComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<BmejegyRecordEntity>(
    "bmejegy-tickets",
    BmejegyRecordEntity::class, ::BmejegyRecordEntity,
    "Jegy", "Jegyek",
    "Ebben a menüben a szinkronizált Bmejegy példányok láthatóak. Az értékek módosíthatóak, mert " +
            "csak az új példányokat húzza be a szinkronizáló mindig.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_BME_TICKET,
    createPermission = StaffPermissions.PERMISSION_CREATE_BME_TICKET,
    editPermission =   StaffPermissions.PERMISSION_EDIT_BME_TICKET,
    deletePermission = StaffPermissions.PERMISSION_DELETE_BME_TICKET,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "local_activity",
    adminMenuPriority = 1,

    searchSettings = calculateSearchSettings<BmejegyRecordEntity>(false)
)
