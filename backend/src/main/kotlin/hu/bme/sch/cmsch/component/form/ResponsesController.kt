package hu.bme.sch.cmsch.component.form

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.TwoDeepEntityPage
import hu.bme.sch.cmsch.extending.FormSubmissionListener
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.getUser
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/control/signup-responses")
@ConditionalOnBean(FormComponent::class)
class ResponsesController(
    responseRepository: ResponseRepository,
    private val formRepository: FormRepository,
    private val formService: FormService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: FormComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    env: Environment,
    transactionManager: PlatformTransactionManager,
    private val listeners: List<FormSubmissionListener>,
    storageService: StorageService
) : TwoDeepEntityPage<FormVirtualEntity, ResponseEntity>(
    "signup-responses",
    FormVirtualEntity::class,
    ResponseEntity::class, ::ResponseEntity,
    "Kitöltés", "Kitöltések",
    "Kitöltések formonként csoportosítva",

    transactionManager,
    object : ManualRepository<FormVirtualEntity, Int>() {
        override fun findAll(): Iterable<FormVirtualEntity> {
            return formService.getAllResponses()
                .groupBy { it.formId }
                .map { it.value }
                .filter { it.isNotEmpty() }
                .mapNotNull { responses ->
                    formRepository.findById(responses[0].formId)
                        .map { form ->
                            FormVirtualEntity(
                                form.id,
                                form.name,
                                form.submissionLimit,
                                responses.size,
                                responses.count { it.accepted },
                                responses.count { it.rejected },
                                responses.count { it.detailsValidated },
                            )
                        }.orElse(null)
                }
        }

    },
    responseRepository,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_FORM_RESULTS,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission =   StaffPermissions.PERMISSION_EDIT_FORM_RESULTS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_FORM_RESULTS,

    createEnabled = false,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = false,
    exportEnabled = false,

    adminMenuIcon = "inbox",
    adminMenuPriority = 2,

    outerControlActions = mutableListOf(
        ControlAction(
            "Json Export",
            "export/json/{id}",
            "data_object",
            StaffPermissions.PERMISSION_SHOW_FORM_RESULTS,
            10,
            true,
            "Exportálás JSON fájlba"
        ),
        ControlAction(
            "CSV Export",
            "export/csv/{id}",
            "save",
            StaffPermissions.PERMISSION_SHOW_FORM_RESULTS,
            20,
            true,
            "Exportálás CSV fájlba"
        )
    )
) {

    private val exportPermission = StaffPermissions.PERMISSION_EDIT_FORM_RESULTS

    override fun fetchSublist(id: Int): Iterable<ResponseEntity> {
        return formService.getResponsesById(id)
    }

    @ResponseBody
    @GetMapping(value = ["/export/csv/{id}"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun exportCsv(@PathVariable id: Int, auth: Authentication): String {
        val user = auth.getUser()
        if (exportPermission.validate(user).not()) {
            return "403"
        }

        val objReader = objectMapper.readerFor(object : TypeReference<Map<String, Any>>() {})
        val entries = formService.getResponsesById(id)
            .map { objReader.readValue<Map<String, Any>>(it.submission) }
            .map { it.values }
            .toList()

        val headers = objReader.readValue<Map<String, Any>>(formService.getResponsesById(id).firstOrNull()?.submission ?: "{}")
            .keys
            .joinToString(",")
        val result = CsvMapper().writeValueAsString(entries)

        return headers + "\n" + result
    }

    @ResponseBody
    @GetMapping(value = ["/export/json/{id}"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun exportJson(@PathVariable id: Int, auth: Authentication): String {
        val user = auth.getUser()
        if (exportPermission.validate(user).not()) {
            return "403"
        }

        val entries = formService.getResponsesById(id).joinToString(",") { it.submission }

        return "[${entries}]"
    }

    override fun onEntityDeleted(entity: ResponseEntity) {
        listeners.forEach { it.onResponseDeleted(entity) }
    }

    override fun onEntitiesPurged() {
        listeners.forEach { it.onResponsesPurged() }
    }

}
