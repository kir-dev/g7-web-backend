package hu.bme.sch.cmsch.component.event

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.ImportService
import hu.bme.sch.cmsch.service.StaffPermissions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/events")
@ConditionalOnBean(EventComponent::class)
class EventController(
    repo: EventRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: EventComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    env: Environment
) : OneDeepEntityPage<EventEntity>(
    "events",
    EventEntity::class, ::EventEntity,
    "Esemény", "Események",
    "A rendezvény összes (publikus) programjainak kezelse.",

    repo,
    importService,
    adminMenuService,
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

    adminMenuIcon = "event",
    adminMenuPriority = 1,
)

