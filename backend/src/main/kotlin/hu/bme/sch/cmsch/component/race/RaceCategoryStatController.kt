package hu.bme.sch.cmsch.component.race

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.config.OwnershipType
import hu.bme.sch.cmsch.config.StartupPropertyConfig
import hu.bme.sch.cmsch.controller.admin.ControlAction
import hu.bme.sch.cmsch.controller.admin.TwoDeepEntityPage
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
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

@Controller
@RequestMapping("/admin/control/race-category-stat")
@ConditionalOnBean(RaceComponent::class)
class RaceCategoryStatController(
    private val raceCategoryRepository: RaceCategoryRepository,
    private val raceService: RaceService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: RaceComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    private val startupPropertyConfig: StartupPropertyConfig,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : TwoDeepEntityPage<RaceCategoryEntity, RaceEntryDto>(
    "race-category-stat",
    RaceCategoryEntity::class,
    RaceEntryDto::class, ::RaceEntryDto,
    "Eredmény", "Extra mérések",
    "Időmérő eredmények nyers időeredményei",

    transactionManager,
    raceCategoryRepository,
    object : ManualRepository<RaceEntryDto, Int>() {},
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_EDIT_RACE_CATEGORY,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission =   ImplicitPermissions.PERMISSION_NOBODY,
    deletePermission = ImplicitPermissions.PERMISSION_NOBODY,

    createEnabled = false,
    editEnabled   = false,
    deleteEnabled = false,
    importEnabled = false,
    exportEnabled = false,

    adminMenuIcon = "leaderboard",
    adminMenuPriority = 3,

    outerControlActions = mutableListOf(
        ControlAction(
            "Json Export",
            "view/{id}/save/csv",
            "save",
            StaffPermissions.PERMISSION_EDIT_RACE_CATEGORY,
            10,
            true,
            "Exportálás JSON fájlba"
        )
    )
) {

    override fun fetchSublist(id: Int): Iterable<RaceEntryDto> {
        val category = raceCategoryRepository.findById(id).orElse(null)
            ?: return listOf()

        return when (startupPropertyConfig.raceOwnershipMode) {
            OwnershipType.USER -> raceService.getBoardForUsers(category.slug, true)
            OwnershipType.GROUP -> raceService.getBoardForGroups(category.slug)
        }
    }

    @ResponseBody
    @GetMapping("/view/{id}/save/csv", produces = [ MediaType.APPLICATION_OCTET_STREAM_VALUE ])
    fun saveStatsAsCsv(@PathVariable id: Int, auth: Authentication, response: HttpServletResponse): ByteArray {
        val user = auth.getUser()
        if (!showPermission.validate(user)) {
            throw IllegalStateException("Insufficient permissions")
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"race-category-export-${id}.csv\"")
        return descriptor.exportToCsv(fetchSublist(id).toList()).toByteArray()
    }

}
