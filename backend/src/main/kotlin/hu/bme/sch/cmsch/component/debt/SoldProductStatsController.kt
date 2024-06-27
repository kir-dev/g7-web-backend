package hu.bme.sch.cmsch.component.debt

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.SimpleEntityPage
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_SHOW_SOLD_STATS
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/track-sold")
@ConditionalOnBean(DebtComponent::class)
class SoldProductStatsController(
    productRepository: SoldProductRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: DebtComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : SimpleEntityPage<ProductGroupVirtualEntity>(
    "track-sold",
    ProductGroupVirtualEntity::class, ::ProductGroupVirtualEntity,
    "Eladott termékek", "Eladott termékek",
    "Az eladott termékek mennyiségei típusra rendezve",

    transactionManager,
    { productRepository.findAll()
        .groupBy { it.product }
        .map { ProductGroupVirtualEntity(0, it.key, it.value.size) } },

    permission = PERMISSION_SHOW_SOLD_STATS,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "receipt_long",
    adminMenuPriority = 5,
)
