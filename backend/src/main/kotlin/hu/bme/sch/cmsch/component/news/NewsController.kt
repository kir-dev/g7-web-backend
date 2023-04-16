package hu.bme.sch.cmsch.component.news

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
@RequestMapping("/admin/control/news")
@ConditionalOnBean(NewsComponent::class)
class NewsController(
    repo: NewsRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: NewsComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper
) : OneDeepEntityPage<NewsEntity>(
    "news",
    NewsEntity::class, ::NewsEntity,
    "Hír", "Hírek",
    "A oldalon megjelenő hírek kezelése.",

    repo,
    importService,
    adminMenuService,
    component,
    auditLog,
    objectMapper,

    showPermission =   StaffPermissions.PERMISSION_SHOW_NEWS,
    createPermission = StaffPermissions.PERMISSION_CREATE_NEWS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_NEWS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_NEWS,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "newspaper",
    adminMenuPriority = 1,
)

