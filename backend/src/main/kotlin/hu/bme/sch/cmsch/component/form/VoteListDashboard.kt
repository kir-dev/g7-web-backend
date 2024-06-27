package hu.bme.sch.cmsch.component.form

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.admin.GenerateOverview
import hu.bme.sch.cmsch.admin.OVERVIEW_TYPE_ID
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.SimpleEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.model.IdentifiableEntity
import hu.bme.sch.cmsch.model.RoleType
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping


data class FormVotesDto(
    @property:GenerateOverview(renderer = OVERVIEW_TYPE_ID, columnName = "ID", order = -1)
    override var id: Int = 0,

    @property:GenerateOverview(columnName = "Űrlap neve", order = 1, useForSearch = true)
    var name: String = "",

    @property:GenerateOverview(columnName = "Kitöltések", order = 2, useForSearch = true)
    var submissions: Long = 0,

) : IdentifiableEntity

@Controller
@RequestMapping("/admin/control/form-votes")
@ConditionalOnBean(FormComponent::class)
class VoteListDashboard(
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: FormComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    env: Environment,
    storageService: StorageService,
    private val formService: FormService,
    transactionManager: PlatformTransactionManager
) : SimpleEntityPage<FormVotesDto>(
    "form-votes",
    FormVotesDto::class, ::FormVotesDto,
    "Szavazások", "Szavazások",
    "Űrlaponként csoportosítva az összes szavazás eredménye. " +
            "Az eredmények azt mutatják, hogy felhasználó szavazott az egyes opcióra. " +
            "Részletes kiértékeléshez töltsd le a válaszokat a kitöltések menüből!",

    transactionManager,
    {
        formService.getAllForms(RoleType.SUPERUSER).map {
            FormVotesDto(
                id = it.id,
                name = it.name,
                submissions = formService.getSubmissionCount(it)
            )
        }
    },

    permission = StaffPermissions.PERMISSION_SHOW_FORM_RESULTS,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "thumbs_up_down",
    adminMenuPriority = 3,

    controlActions = mutableListOf(
        ControlAction(
            name = "Szavazatok",
            endpoint = "votes/{id}",
            icon = "thumbs_up_down",
            permission = StaffPermissions.PERMISSION_SHOW_FORM_RESULTS,
            order = 10,
            newPage = false,
            usageString = "Értékelés megnyitása"
        )
    ),
    searchSettings = calculateSearchSettings<FormVotesDto>(false)
) {

    @GetMapping("/votes/{id}")
    fun redirect(@PathVariable id: Int): String {
        return "redirect:/admin/control/form-vote/${id}"
    }

}
