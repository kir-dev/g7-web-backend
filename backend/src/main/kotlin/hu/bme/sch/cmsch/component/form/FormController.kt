package hu.bme.sch.cmsch.component.form

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.component.sheets.SHEETS_WIZARD
import hu.bme.sch.cmsch.component.sheets.SheetsComponent
import hu.bme.sch.cmsch.component.sheets.SheetsUpdaterService
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ImportService
import hu.bme.sch.cmsch.service.StaffPermissions
import hu.bme.sch.cmsch.util.transaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Controller
@RequestMapping("/admin/control/forms")
@ConditionalOnBean(FormComponent::class)
class FormController(
    repo: FormRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: FormComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService,
    sheetsComponent: Optional<SheetsComponent>,
    private val formRepository: FormRepository,
    private val sheetsUpdaterService: Optional<SheetsUpdaterService>
) : OneDeepEntityPage<FormEntity>(
    "forms",
    FormEntity::class, ::FormEntity,
    "Űrlap", "Űrlapok",
    "Űrlapok az eseményre vagy annak részprogramjára való jelentkezéshez.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_FORM,
    createPermission = StaffPermissions.PERMISSION_CREATE_FORM,
    editPermission =   StaffPermissions.PERMISSION_EDIT_FORM,
    deletePermission = StaffPermissions.PERMISSION_DELETE_FORM,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "view_list",
    adminMenuPriority = 1,

    controlActions = controlActionsBuilder(sheetsComponent.isPresent),

    searchSettings = calculateSearchSettings<FormEntity>(false)
) {

    @GetMapping("/sheets/{id}")
    fun sheets(@PathVariable id: Int): String {
        val name = transactionManager.transaction(readOnly = true) {
            return@transaction formRepository.findById(id).getOrNull()?.name ?: "Névtelen"
        }
        return "redirect:/admin/control/$SHEETS_WIZARD/form/${id}?name=${URLEncoder.encode("$name űrlap", StandardCharsets.UTF_8)}"
    }

    @GetMapping("/fill/{id}")
    fun fill(@PathVariable id: Int): String {
        return "redirect:/admin/control/$FORM_MASTER_FILL/form/${id}"
    }

    @GetMapping("/refresh/{id}")
    fun refresh(@PathVariable id: Int): String {
        return sheetsUpdaterService.orElseThrow().updateAllSheetsByForm(id)
    }

}

private fun controlActionsBuilder(sheetsEnabled: Boolean): MutableList<ControlAction> {
    val actions = mutableListOf<ControlAction>()
    actions.add(
        ControlAction(
            name = "Kitöltés",
            endpoint = "fill/{id}",
            icon = "edit_note",
            permission = StaffPermissions.PERMISSION_EDIT_FORM,
            order = 175,
            usageString = "Manuális kitöltés",
        )
    )
    if (sheetsEnabled) {
        actions.add(
            ControlAction(
                name = "Sheets integráció",
                endpoint = "sheets/{id}",
                icon = "dataset_linked",
                permission = StaffPermissions.PERMISSION_CREATE_SHEETS,
                order = 180,
                usageString = "Összekapcsolás Google Sheetssel",
            )
        )
        actions.add(
            ControlAction(
                name = "Sheets frissítése",
                endpoint = "refresh/{id}",
                icon = "cloud_sync",
                permission = StaffPermissions.PERMISSION_CREATE_SHEETS,
                order = 190,
                usageString = "Összes csatolt Google Sheets frissítése",
            )
        )
    }
    return actions
}
