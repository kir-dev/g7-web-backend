package hu.bme.sch.cmsch.component.riddle

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.ImportService
import hu.bme.sch.cmsch.service.StaffPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/riddle-categories")
@ConditionalOnBean(RiddleComponent::class)
class RiddleCategoryController(
    repo: RiddleCategoryRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: RiddleComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper
) : OneDeepEntityPage<RiddleCategoryEntity>(
    "riddle-categories",
    RiddleCategoryEntity::class, ::RiddleCategoryEntity,
    "Riddle Kategória", "Riddle Kategóriák",
    "Képrejtvény kategóriák kezelése.",

    repo,
    importService,
    adminMenuService,
    component,
    auditLog,
    objectMapper,

    showPermission =   StaffPermissions.PERMISSION_SHOW_RIDDLE_CATEGORIES,
    createPermission = StaffPermissions.PERMISSION_CREATE_RIDDLE_CATEGORIES,
    editPermission =   StaffPermissions.PERMISSION_EDIT_RIDDLE_CATEGORIES,
    deletePermission = StaffPermissions.PERMISSION_DELETE_RIDDLE_CATEGORIES,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "category",
    adminMenuPriority = 2,
)
