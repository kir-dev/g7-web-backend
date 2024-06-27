package hu.bme.sch.cmsch.component.gallery

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
@RequestMapping("/admin/control/gallery")
@ConditionalOnBean(GalleryComponent::class)
class GalleryController(
    repo: GalleryRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: GalleryComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<GalleryEntity>(
    "gallery",
    GalleryEntity::class, ::GalleryEntity,
    "Kép", "Galéria",
    "A rendezvény galériájának kezelése.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_GALLERY,
    createPermission = StaffPermissions.PERMISSION_CREATE_GALLERY,
    editPermission =   StaffPermissions.PERMISSION_EDIT_GALLERY,
    deletePermission = StaffPermissions.PERMISSION_DELETE_GALLERY,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "collections",
    adminMenuPriority = 1,
    searchSettings = calculateSearchSettings<GalleryEntity>(true)
)
