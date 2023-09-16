package hu.bme.sch.cmsch.component.key

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.ImportService
import hu.bme.sch.cmsch.service.StaffPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/access-key")
@ConditionalOnBean(AccessKeyComponent::class)
class AccessKeyController(
    repo: AccessKeyRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: AccessKeyComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment
) : OneDeepEntityPage<AccessKeyEntity>(
    "access-key",
    AccessKeyEntity::class, ::AccessKeyEntity,
    "Hozzáférési kulcs", "Hozzáférési kulcsok",
    "Ezekkel a kulcsokkal lehet hozzáférést adni csoportokhoz (group) és szerepekhez (role).",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_ACCESS_KEYS,
    createPermission = StaffPermissions.PERMISSION_CREATE_ACCESS_KEYS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_ACCESS_KEYS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_ACCESS_KEYS,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "key",
    adminMenuPriority = 1,
)

