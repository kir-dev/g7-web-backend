package hu.bme.sch.cmsch.component.debt

import hu.bme.sch.cmsch.admin.GenerateOverview
import hu.bme.sch.cmsch.admin.OverviewBuilder
import hu.bme.sch.cmsch.controller.CONTROL_MODE_EDIT_DELETE
import hu.bme.sch.cmsch.controller.admin.EXPERIMENTAL_CATEGORY
import hu.bme.sch.cmsch.model.ManagedEntity
import hu.bme.sch.cmsch.service.AdminMenuEntry
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.TimeService
import hu.bme.sch.cmsch.service.ExperimentalPermissions.PERMISSION_EXP_TRANSACTION_IMPORT
import hu.bme.sch.cmsch.util.getUser
import hu.bme.sch.cmsch.util.getUserFromDatabase
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.annotation.PostConstruct
import kotlin.reflect.KProperty1

@Controller
@RequestMapping("/admin/control/import-debts-complete")
@ConditionalOnBean(DebtComponent::class)
class ImportDebtCompleteController(
    private val transactions: SoldProductRepository,
    private val clock: TimeService,
    private val adminMenuService: AdminMenuService
) {

    private val view = "import-debts-complete"
    private val titleSingular = "Import"
    private val titlePlural = "Tranzakció import"
    private val description = "Tartozások importálása, experimental és nem transactional!"
    private val permissionControl = PERMISSION_EXP_TRANSACTION_IMPORT

    private val entitySourceMapping: Map<String, (SoldProductEntity) -> List<String>> =
            mapOf(Nothing::class.simpleName!! to { listOf() })

    private val descriptor = OverviewBuilder(ImportDebtsCompleteVirtualEntity::class)

    @PostConstruct
    fun init() {
        adminMenuService.registerEntry(
            EXPERIMENTAL_CATEGORY, AdminMenuEntry(
                titlePlural,
                "upload_file",
                "/admin/control/${view}",
                1,
                permissionControl
            )
        )
    }

    @GetMapping("")
    fun view(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (permissionControl.validate(user).not()) {
            model.addAttribute("permission", permissionControl.permissionString)
            model.addAttribute("user", user)
            return "admin403"
        }

        model.addAttribute("title", titlePlural)
        model.addAttribute("titleSingular", titleSingular)
        model.addAttribute("description", description)
        model.addAttribute("view", view)
        model.addAttribute("columns", listOf<String>())
        model.addAttribute("fields", listOf<Pair<KProperty1<out Any, *>, GenerateOverview>>())
        model.addAttribute("rows", listOf<ManagedEntity>())
        model.addAttribute("user", user)
        model.addAttribute("controlMode", CONTROL_MODE_EDIT_DELETE)

        return "overview"
    }

    @GetMapping("/create")
    fun create(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (permissionControl.validate(user).not()) {
            model.addAttribute("permission", permissionControl.permissionString)
            model.addAttribute("user", user)
            return "admin403"
        }

        model.addAttribute("title", titleSingular)
        model.addAttribute("editMode", false)
        model.addAttribute("view", view)
        model.addAttribute("inputs", descriptor.getInputs())
        model.addAttribute("mappings", entitySourceMapping)
        model.addAttribute("data", null)
        model.addAttribute("user", user)
        model.addAttribute("controlMode", CONTROL_MODE_EDIT_DELETE)

        return "details"
    }

    @ResponseBody
    @PostMapping("/create")
    fun create(
        @ModelAttribute(binding = false) dto: ImportDebtsCompleteVirtualEntity,
        model: Model,
        auth: Authentication
    ): String {
        val user = auth.getUserFromDatabase()
        if (permissionControl.validate(user).not()) {
            model.addAttribute("user", user)
            return "403"
        }

        return dto.transactionIds.split(Regex("[\\n\\r]"))
                .asSequence()
                .filter { it.isNotBlank() }
                .map { transaction ->
                    println("Setting #$transaction")
                    return@map transactions.findById(transaction.toInt()).map {
                        if (it.payed && it.finsihed) {
                            println("Already set #${it.id}")
                            "${it.id}: already;"
                        } else {
                            it.payed = true
                            it.payedAt = clock.getTimeInSeconds()
                            it.finsihed = true
                            it.log += " autoclose by importer: ${user.fullName} at ${clock.getTimeInSeconds()};"
                            transactions.save(it)
                            println("Closed #${it.id}")
                            "${it.id}: closed;"
                        }
                    }.orElse("$transaction: not found;")
                }
                .joinToString("\n")

    }

}
