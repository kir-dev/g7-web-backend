package hu.bme.sch.cmsch.component.race

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.config.OwnershipType
import hu.bme.sch.cmsch.config.StartupPropertyConfig
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_EDIT_RACE
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/race-leaderboard")
@ConditionalOnBean(RaceComponent::class)
class RaceLeaderBoardController(
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: RaceComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    private val raceService: RaceService,
    private val startupPropertyConfig: StartupPropertyConfig,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<RaceEntryDto>(
    "race-leaderboard",
    RaceEntryDto::class, ::RaceEntryDto,
    "Mérés toplista", "Mérés toplista",
    "Toplista a mérési eredmények alapján",

    transactionManager,
    object : ManualRepository<RaceEntryDto, Int>() {

        override fun findAll(): Iterable<RaceEntryDto> {
            return when (startupPropertyConfig.raceOwnershipMode) {
                OwnershipType.USER -> raceService.getBoardForUsers(DEFAULT_CATEGORY, true)
                OwnershipType.GROUP -> raceService.getBoardForGroups(DEFAULT_CATEGORY)
            }
        }

        override fun count() = findAll().count().toLong()

    },

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission = PERMISSION_EDIT_RACE,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission =   ImplicitPermissions.PERMISSION_NOBODY,
    deletePermission = ImplicitPermissions.PERMISSION_NOBODY,

    createEnabled = false,
    editEnabled   = false,
    deleteEnabled = false,
    importEnabled = false,
    exportEnabled = true,

    adminMenuIcon = "leaderboard",
    adminMenuPriority = 5,

    searchSettings = calculateSearchSettings<RaceEntryDto>(false)
)
