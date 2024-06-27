package hu.bme.sch.cmsch.component.sheets

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ImportService
import hu.bme.sch.cmsch.service.StaffPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/sheets")
@ConditionalOnBean(SheetsComponent::class)
class SheetsController(
    repo: SheetsRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: SheetsComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<SheetsEntity>(
    "sheets",
    SheetsEntity::class, ::SheetsEntity,
    "Integráció", "Integrációk",
    "Itt tudod kezelni az aktív Google Sheets integrációkat. Létrehozáshoz használd a varázslót a kiválasztott komponensben!",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_SHEETS,
    createPermission = StaffPermissions.PERMISSION_CREATE_SHEETS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_SHEETS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_SHEETS,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "integration_instructions",
    adminMenuPriority = 1,

    searchSettings = calculateSearchSettings<SheetsEntity>(false)
)
