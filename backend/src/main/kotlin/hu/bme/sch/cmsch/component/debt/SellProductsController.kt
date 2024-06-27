package hu.bme.sch.cmsch.component.debt

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.SimpleEntityPage
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_SELL_ANY_PRODUCT
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_SELL_FOOD
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_SELL_MERCH
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

private const val SELL_PRODUCT_TITLE = "Termék árusítás"
private const val SELL_FOOD_TITLE = "Étel árusítás"
private const val SELL_MERCH_TITLE = "Merch árusítás"

private const val DESCRIPTION = "Válassz terméket az eladáshoz"

@Controller
@RequestMapping("/admin/control/sell-products")
@ConditionalOnBean(DebtComponent::class)
class SellProductsController(
    private val productService: ProductService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: DebtComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : SimpleEntityPage<ProductEntity>(
    "sell-products",
    ProductEntity::class, ::ProductEntity,
    SELL_PRODUCT_TITLE, SELL_PRODUCT_TITLE,
    DESCRIPTION,

    transactionManager,
    { _ -> productService.getAllProducts() },

    permission = PERMISSION_SELL_ANY_PRODUCT,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "point_of_sale",
    adminMenuPriority = 2,

    controlActions = mutableListOf(
        ControlAction(
            "Árusít",
            "sell/{id}",
            "sell",
            PERMISSION_SELL_ANY_PRODUCT,
            100,
            false,
            "Árusító oldal megnyitása"
        )
    )
) {

    @GetMapping("/sell/{id}")
    fun redirectSelling(@PathVariable id: String): String {
        return "redirect:/admin/sell/${id}"
    }

}

@Controller
@RequestMapping("/admin/control/sell-food")
@ConditionalOnBean(DebtComponent::class)
class SellFoodController(
    private val productService: ProductService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: DebtComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : SimpleEntityPage<ProductEntity>(
    "sell-food",
    ProductEntity::class, ::ProductEntity,
    SELL_FOOD_TITLE, SELL_FOOD_TITLE,
    DESCRIPTION,

    transactionManager,
    { _ -> productService.getAllFoods() },

    permission = PERMISSION_SELL_FOOD,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "restaurant",
    adminMenuPriority = 3,

    controlActions = mutableListOf(
        ControlAction(
            "Árusít",
            "sell/{id}",
            "sell",
            PERMISSION_SELL_FOOD,
            100,
            false,
            "Árusító oldal megnyitása"
        )
    )
) {

    @GetMapping("/sell/{id}")
    fun redirectSelling(@PathVariable id: String): String {
        return "redirect:/admin/sell/${id}"
    }

}

@Controller
@RequestMapping("/admin/control/sell-merch")
@ConditionalOnBean(DebtComponent::class)
class SellMerchController(
    private val productService: ProductService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: DebtComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : SimpleEntityPage<ProductEntity>(
    "sell-merch",
    ProductEntity::class, ::ProductEntity,
    SELL_MERCH_TITLE, SELL_MERCH_TITLE,
    DESCRIPTION,

    transactionManager,
    { _ -> productService.getAllMerch() },

    permission = PERMISSION_SELL_MERCH,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "sell",
    adminMenuPriority = 4,

    controlActions = mutableListOf(
        ControlAction(
            "Árusít",
            "sell/{id}",
            "sell",
            PERMISSION_SELL_MERCH,
            100,
            false,
            "Árusító oldal megnyitása"
        )
    )
) {

    @GetMapping("/sell/{id}")
    fun redirectSelling(@PathVariable id: String): String {
        return "redirect:/admin/sell/${id}"
    }

}
