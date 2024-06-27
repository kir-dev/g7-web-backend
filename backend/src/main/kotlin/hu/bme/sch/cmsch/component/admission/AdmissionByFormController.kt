package hu.bme.sch.cmsch.component.admission

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.admin.GenerateOverview
import hu.bme.sch.cmsch.admin.OVERVIEW_TYPE_ID
import hu.bme.sch.cmsch.component.form.FormRepository
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.SimpleEntityPage
import hu.bme.sch.cmsch.model.IdentifiableEntity
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.StorageService
import hu.bme.sch.cmsch.service.ImportService
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_EXPORT_ADMISSION
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_VALIDATE_ADMISSION
import hu.bme.sch.cmsch.util.getUser
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.util.*

data class AdmissionFormEntry(

    @property:GenerateOverview(renderer = OVERVIEW_TYPE_ID, columnName = "ID", order = -1)
    override var id: Int = 0,

    @property:GenerateOverview(columnName = "Form neve", order = 1)
    var name: String = "",

    @property:GenerateOverview(columnName = "Form url", order = 2)
    var url: String = "",

) : IdentifiableEntity

@Controller
@RequestMapping("/admin/control/admission-by-form")
@ConditionalOnBean(AdmissionComponent::class)
class AdmissionByFormController(
    private val formRepository: Optional<FormRepository>,
    private val admissionService: AdmissionService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: AdmissionComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : SimpleEntityPage<AdmissionFormEntry>(
    "admission-by-form",
    AdmissionFormEntry::class, ::AdmissionFormEntry,
    "Űrlapos beléptetés", "Űrlapos beléptetés",
    "${if (formRepository.isEmpty) "FORM KOMPONENS NINCS BETÖLTVE! " else ""}Felhasználó tulajdonú űrlapok alapján beengedés",

    transactionManager,
    {
        formRepository.map { repository ->
            repository.findAll()
                    .filter { !it.ownerIsGroup }
                    .map { form -> AdmissionFormEntry(form.id, form.name, form.url) }
        }.orElse(listOf())
    },

    permission = PERMISSION_VALIDATE_ADMISSION,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "mobile_friendly",
    adminMenuPriority = 2,

    controlActions = mutableListOf(
        ControlAction(
            "Beengedés",
            "scan/{id}",
            "qr_code_scanner",
            PERMISSION_VALIDATE_ADMISSION,
            100,
            true,
            "Beengedő felület megnyitása"
        ),
        ControlAction(
            "CSV Export",
            "export/csv/{id}",
            "save",
            PERMISSION_EXPORT_ADMISSION,
            200,
            true,
            "Mentés CSV-be"
        )
    )
) {

    val exportPermission = PERMISSION_EXPORT_ADMISSION

    @GetMapping("/scan/{id}")
    fun generatePdf(@PathVariable id: Int, auth: Authentication, response: HttpServletResponse): String {
        if (showPermission.validate(auth.getUser()).not()) {
            return "redirect:/admin/control/admission-by-form?error=access"
        }
        return "redirect:/admin/admission/form/${id}"
    }

    data class AdmissionExportDto(
        var formId: Int = 0,
        var responseId: Int = 0,
        var userId: Int = 0,
        var userName: String = "",
        var joined: Boolean = false,
        var response: String = "",
        var timestamp: Long = 0,
        var grant: String = ""
    )

    @ResponseBody
    @GetMapping("/export/csv/{id}", produces = [ MediaType.APPLICATION_OCTET_STREAM_VALUE ])
    fun export(auth: Authentication, response: HttpServletResponse, @PathVariable id: Int): ByteArray {
        val user = auth.getUser()
        if (!exportPermission.validate(user)) {
            throw IllegalStateException("Insufficient permissions")
        }

        val outputStream = admissionService.generateAdmissionExportForForm(id)

        response.setHeader("Content-Disposition", "attachment; filename=\"form-export-${id}.csv\"")
        return outputStream.toByteArray()
    }

}
