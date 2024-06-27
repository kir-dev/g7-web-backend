package hu.bme.sch.cmsch.component.admission

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
@RequestMapping("/admin/control/admission-entries")
@ConditionalOnBean(AdmissionComponent::class)
class AdmissionController(
    repo: AdmissionEntryRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: AdmissionComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    env: Environment,
    transactionManager: PlatformTransactionManager,
    storageService: StorageService,
) : OneDeepEntityPage<AdmissionEntryEntity>(
    "admission-entries",
    AdmissionEntryEntity::class, ::AdmissionEntryEntity,
    "Belépés log", "Belépés logok",
    "Az összes rögzített beléptetés",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_ADMISSIONS,
    createPermission = StaffPermissions.PERMISSION_CREATE_ADMISSIONS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_ADMISSIONS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_ADMISSIONS,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "fact_check",
    adminMenuPriority = 3,
    searchSettings = calculateSearchSettings<AdmissionEntryEntity>(true)
)
