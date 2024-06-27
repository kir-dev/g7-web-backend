package hu.bme.sch.cmsch.component.team

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/admin/control/team-introductions")
@ConditionalOnBean(TeamComponent::class)
class TeamIntroductionReviewController(
    private val teamIntroductionRepository: TeamIntroductionRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: TeamComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<TeamIntroductionEntity>(
    "team-introductions",
    TeamIntroductionEntity::class, ::TeamIntroductionEntity,
    "Bemutatkozás", "Bemutatkozások",
    "Csapatok bemutatkozásainak ellenőrzése",

    transactionManager,
    teamIntroductionRepository,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission = StaffPermissions.PERMISSION_SHOW_TEAM_INTRODUCTIONS,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission = StaffPermissions.PERMISSION_EDIT_TEAM_INTRODUCTIONS,
    deletePermission = ImplicitPermissions.PERMISSION_NOBODY,

    createEnabled = false,
    editEnabled = true,
    deleteEnabled = false,
    importEnabled = false,
    exportEnabled = false,

    adminMenuIcon = "rate_review",
    adminMenuPriority = 2,

    searchSettings = calculateSearchSettings<TeamIntroductionEntity>(false)
) {
    override fun fetchOverview(user: CmschUser): Iterable<TeamIntroductionEntity> {
        return teamIntroductionRepository.findAll()
            .sortedWith(
                compareBy<TeamIntroductionEntity> { it.groupName }.thenComparing { it.creationDate }.reversed()
            )
    }
}
