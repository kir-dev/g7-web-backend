package hu.bme.sch.cmsch.component.riddle

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.TwoDeepEntityPage
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.repository.UserRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.transaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping
import kotlin.jvm.optionals.getOrNull

@Controller
@RequestMapping("/admin/control/riddles-by-users")
@ConditionalOnBean(RiddleComponent::class)
class RiddlesByUsersController(
    private val riddleMappingRepository: RiddleMappingRepository,
    private val riddleRepository: RiddleEntityRepository,
    private val userRepository: UserRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: RiddleComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : TwoDeepEntityPage<RiddleStatsVirtualEntity, RiddleMappingVirtualEntity>(
    "riddles-by-users",
    RiddleStatsVirtualEntity::class,
    RiddleMappingVirtualEntity::class, ::RiddleMappingVirtualEntity,
    "Riddle beadás felhasználónként", "Riddle felhasználónként",
    "Beadott riddleök felhasználónként csoportosítva",

    transactionManager,
    object : ManualRepository<RiddleStatsVirtualEntity, Int>() {
        override fun findAll(): Iterable<RiddleStatsVirtualEntity> {
            return transactionManager.transaction(readOnly = true) { riddleMappingRepository.findAll() }
                .groupBy { it.ownerUserId }
                .map { it.value }
                .filter { it.isNotEmpty() }
                .map { submissions ->
                    val user = userRepository.findById(submissions[0].ownerUserId).getOrNull()
                    RiddleStatsVirtualEntity(
                        submissions[0].ownerUserId,
                        user?.fullName ?: "n/a",
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
    adminMenuPriority = 3,
) {

    override fun fetchSublist(id: Int): Iterable<RiddleMappingVirtualEntity> {
        return riddleMappingRepository.findAllByOwnerUserId(id)
            .map { submission ->
                val riddle = riddleRepository.findById(submission.riddleId).getOrNull()
                RiddleMappingVirtualEntity(
                    submission.id,
                    riddle?.categoryId ?: 0,
                    riddle?.title ?: "n/a",
                    submission.hintUsed,
                    submission.completed,
                    submission.skipped,
                    submission.attemptCount,
                    submission.completedAt
                )
            }
    }

}
