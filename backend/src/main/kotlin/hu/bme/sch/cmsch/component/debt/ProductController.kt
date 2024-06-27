package hu.bme.sch.cmsch.component.debt

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
@RequestMapping("/admin/control/products")
@ConditionalOnBean(DebtComponent::class)
class ProductController(
    repo: ProductRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: DebtComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<ProductEntity>(
    "products",
    ProductEntity::class, ::ProductEntity,
    "Termék", "Termékek",
    "Az összes vásárolható termék kezelése. Az eladáshoz külön felület tartozik!",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_PRODUCTS,
    createPermission = StaffPermissions.PERMISSION_CREATE_PRODUCTS,
    editPermission =   StaffPermissions.PERMISSION_EDIT_PRODUCTS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_PRODUCTS,

    createEnabled = true,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "inventory_2",
    adminMenuPriority = 1,

    searchSettings = calculateSearchSettings<ProductEntity>(false)
)
