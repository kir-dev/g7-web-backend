package hu.bme.sch.cmsch.component.news

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
@RequestMapping("/admin/control/news")
@ConditionalOnBean(NewsComponent::class)
class NewsController(
    repo: NewsRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: NewsComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<NewsEntity>(
    "news",
    NewsEntity::class, ::NewsEntity,
    "Hír", "Hírek",
    "A oldalon megjelenő hírek kezelése.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

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

    searchSettings = calculateSearchSettings<NewsEntity>(false)
)
