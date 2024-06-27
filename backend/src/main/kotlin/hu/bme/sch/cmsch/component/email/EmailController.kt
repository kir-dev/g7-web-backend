package hu.bme.sch.cmsch.component.email

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.component.RealEntityController
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/email-templates")
@ConditionalOnBean(EmailComponent::class)
class EmailController(
    override val repo: EmailTemplateRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: EmailComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<EmailTemplateEntity>(
    "email-templates",
    EmailTemplateEntity::class, ::EmailTemplateEntity,
    "Email sablon", "Email sablonok",
    "Email sablonok amiket az oldal különböző komponensei tudnak használni.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_EVENTS,
    createPermission = StaffPermissions.PERMISSION_CREATE_EVENTS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_EVENTS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_EVENTS,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "mail",
    adminMenuPriority = 1,
    searchSettings = calculateSearchSettings<EmailTemplateEntity>(true)
), RealEntityController
