package hu.bme.sch.cmsch.component.riddle

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.admin.*
import hu.bme.sch.cmsch.controller.admin.ButtonAction
import hu.bme.sch.cmsch.controller.admin.TwoDeepEntityPage
import hu.bme.sch.cmsch.repository.GroupRepository
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.getUser
import hu.bme.sch.cmsch.util.transaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletResponse
import java.util.*
import org.springframework.transaction.PlatformTransactionManager
import kotlin.jvm.optionals.getOrNull

@Controller
@RequestMapping("/admin/control/riddles-by-groups")
@ConditionalOnBean(RiddleComponent::class)
class RiddlesByGroupsController(
    private val riddleMappingRepository: RiddleMappingRepository,
    private val riddleRepository: RiddleEntityRepository,
    private val groupRepository: GroupRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: RiddleComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : TwoDeepEntityPage<RiddleStatsVirtualEntity, RiddleMappingVirtualEntity>(
    "riddles-by-groups",
    RiddleStatsVirtualEntity::class,
    RiddleMappingVirtualEntity::class, ::RiddleMappingVirtualEntity,
    "Riddle beadás csoportonként", "Riddle csoportonként",
    "Beadott riddleök csoportonként csoportosítva",

    transactionManager,
    object : ManualRepository<RiddleStatsVirtualEntity, Int>() {
        override fun findAll(): Iterable<RiddleStatsVirtualEntity> {
            return transactionManager.transaction(readOnly = true) { riddleMappingRepository.findAll() }
                .groupBy { it.ownerGroupId }
                .map { it.value }
                .filter { it.isNotEmpty() }
                .map { submissions ->
                    val group = groupRepository.findById(submissions[0].ownerGroupId).getOrNull()
                    RiddleStatsVirtualEntity(
                        submissions[0].ownerGroupId,
                        group?.name ?: "n/a",
                        submissions.count { it.completed },
                        submissions.count { it.hintUsed }
                    )
                }
        }

        override fun delete(entity: RiddleStatsVirtualEntity) {
            riddleMappingRepository.deleteById(entity.id)
        }

        override fun deleteAll() {
            riddleMappingRepository.deleteAll()
        }
    },
    object : ManualRepository<RiddleMappingVirtualEntity, Int>() {
        override fun findById(id: Int): Optional<RiddleMappingVirtualEntity> {
            return riddleMappingRepository.findById(id).map { it.toVirtualEntity(riddleRepository) }
        }

        override fun delete(entity: RiddleMappingVirtualEntity) {
            riddleMappingRepository.deleteById(entity.id)
        }

        override fun deleteAll() {
            riddleMappingRepository.deleteAll()
        }
    },
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_RIDDLE_SUBMISSIONS,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission =   ImplicitPermissions.PERMISSION_NOBODY,
    deletePermission = StaffPermissions.PERMISSION_DELETE_RIDDLE_SUBMISSIONS,

    showEnabled   = true,
    createEnabled = false,
    editEnabled   = false,
    deleteEnabled = true,
    importEnabled = false,
    exportEnabled = false,

    adminMenuIcon = "checklist_rtl",
    adminMenuPriority = 4,

    buttonActions = mutableListOf(
        ButtonAction(
            name = "Riddle statisztika export",
            "filtered-export/csv",
            StaffPermissions.PERMISSION_EDIT_GROUPS,
            300,
            icon = "export_notes",
            primary = false,
            newPage = true
        )
    )
) {

    override fun fetchSublist(id: Int): Iterable<RiddleMappingVirtualEntity> {
        return riddleMappingRepository.findAllByOwnerGroupId(id)
            .map { submission -> submission.toVirtualEntity(riddleRepository) }
    }

    data class RiddleByGroupFilteredView(
        @property:ImportFormat
        var riddleId: Int = 0,

        @property:ImportFormat
        var riddleName: String = "",

        @property:ImportFormat
        var groupId: Int = 0,

        @property:ImportFormat
        var groupName: String = "",

        @property:ImportFormat
        var score: Int = 0,

        @property:ImportFormat
        var hint: Boolean = false,

        @property:ImportFormat
        var completed: Boolean = false,

        @property:ImportFormat
        var skipped: Boolean = false,

        @property:ImportFormat
        var attemptCount: Int = 0,
    )

    private val filterDescriptor = OverviewBuilder(RiddleByGroupFilteredView::class)

    @ResponseBody
    @GetMapping("/filtered-export/csv", produces = [ MediaType.APPLICATION_OCTET_STREAM_VALUE ])
    fun filteredExport(auth: Authentication, response: HttpServletResponse): ByteArray {
        val user = auth.getUser()
        if (!showPermission.validate(user)) {
            throw IllegalStateException("Insufficient permissions")
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"$view-filtered-export.csv\"")
        return filterDescriptor.exportToCsv(riddleMappingRepository.findAll()
            .filter { it.completed }
            .map {
                val riddle = riddleRepository.findById(it.riddleId).getOrNull()
                val group = groupRepository.findById(it.ownerGroupId).getOrNull()
                RiddleByGroupFilteredView(
                    it.riddleId,
                    riddle?.title ?: "-",
                    it.ownerGroupId,
                    group?.name ?: "",
                    riddle?.score ?: 0,
                    it.hintUsed,
                    it.completed,
                    it.skipped,
                    it.attemptCount
                )
            }).toByteArray()
    }
}
