package hu.bme.sch.cmsch.component.debt

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.INVALID_ID_ERROR
import hu.bme.sch.cmsch.controller.admin.SimpleEntityPage
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.ImplicitPermissions.PERMISSION_IMPLICIT_HAS_GROUP
import hu.bme.sch.cmsch.util.getUser
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/debts-of-my-group")
@ConditionalOnBean(DebtComponent::class)
class DebtsOfMyGroupAdminController(
    private val userService: UserService,
    private val productService: ProductService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: DebtComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : SimpleEntityPage<SoldProductEntity>(
    "debts-of-my-group",
    SoldProductEntity::class, ::SoldProductEntity,
    "Tartozás", "Csoportom tartozásai",
    "Ha a tartozáshoz a pénzt odaadta neked a kolléga, akkor pipáld ki itt. " +
            "Onnantól a te felelősséged lesz majd elszámolni a gazdaságisnak.",

    transactionManager,
    { user -> productService.getAllDebtsByGroup(userService.getByUserId(user.id)) },

    permission = PERMISSION_IMPLICIT_HAS_GROUP,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "fact_check",
    adminMenuPriority = 7,

    controlActions = mutableListOf(
        ControlAction(
            "Fizetve",
            "payed/{id}",
            "check_circle_outline",
            PERMISSION_IMPLICIT_HAS_GROUP,
            100,
            false,
            "A tétel fizetve"
        )
    )
) {

    private val payPermission = PERMISSION_IMPLICIT_HAS_GROUP

    @GetMapping("/payed/{id}")
    fun setDebtsStatus(@PathVariable id: Int, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (payPermission.validate(user).not()) {
            model.addAttribute("permission", payPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /$view/payed/$id",
                payPermission.permissionString)
            return "admin403"
        }

        model.addAttribute("title", "Tartozás")
        model.addAttribute("view", view)
        model.addAttribute("id", id)
        model.addAttribute("user", user)

        val entity = productService.findTransactionById(id)
        if (entity.isEmpty) {
            model.addAttribute("error", INVALID_ID_ERROR)
        } else {
            model.addAttribute("item", entity.orElseThrow().toString())
        }
        return "payed"
    }

    @PostMapping("/payed/{id}")
    fun payed(@PathVariable id: Int, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        if (payPermission.validate(user).not()) {
            model.addAttribute("permission", payPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "POST /$view/payed/$id",
                payPermission.permissionString)
            return "admin403"
        }

        productService.setTransactionPayed(id, user)
        return "redirect:/admin/control/debts-of-my-group"
    }

}
