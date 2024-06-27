package hu.bme.sch.cmsch.component.qrfight

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
@RequestMapping("/admin/control/qr-towers")
@ConditionalOnBean(QrFightComponent::class)
class QrTowerController(
    repo: QrTowerRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: QrFightComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<QrTowerEntity>(
    "qr-towers",
    QrTowerEntity::class, ::QrTowerEntity,
    "Torony", "Tornyok",
    "QR Fight toronyok hozzáadása és szerkesztése",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_QR_FIGHT,
    createPermission = StaffPermissions.PERMISSION_CREATE_QR_FIGHT,
    editPermission =   StaffPermissions.PERMISSION_EDIT_QR_FIGHT,
    deletePermission = StaffPermissions.PERMISSION_DELETE_QR_FIGHT,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "cell_tower",
    adminMenuPriority = 2,

    searchSettings = calculateSearchSettings<QrTowerEntity>(false)
)
