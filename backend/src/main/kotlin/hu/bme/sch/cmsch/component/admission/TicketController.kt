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
@RequestMapping("/admin/control/tickets")
@ConditionalOnBean(AdmissionComponent::class)
class TicketController(
    repo: TicketRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: AdmissionComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    env: Environment,
    transactionManager: PlatformTransactionManager,
    storageService: StorageService
) : OneDeepEntityPage<TicketEntity>(
    "tickets",
    TicketEntity::class, ::TicketEntity,
    "Jegy", "Jegyek",
    "Jegyek résztvevők és rendezők számára",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_TICKETS,
    createPermission = StaffPermissions.PERMISSION_CREATE_TICKETS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_TICKETS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_TICKETS,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "local_activity",
    adminMenuPriority = 4,
    searchSettings = calculateSearchSettings<TicketEntity>(false)
)
