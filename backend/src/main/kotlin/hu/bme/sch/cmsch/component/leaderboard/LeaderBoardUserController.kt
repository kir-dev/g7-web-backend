package hu.bme.sch.cmsch.component.leaderboard

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.ButtonAction
import hu.bme.sch.cmsch.controller.admin.SimpleEntityPage
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.ControlPermissions.PERMISSION_CONTROL_LEADERBOARD
import hu.bme.sch.cmsch.util.getUser
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import jakarta.servlet.http.HttpServletResponse
import org.springframework.transaction.PlatformTransactionManager

@Controller
@RequestMapping("/admin/control/user-toplist")
@ConditionalOnBean(LeaderBoardComponent::class)
class LeaderBoardUserController(
    private val leaderBoardService: LeaderBoardService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: LeaderBoardComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : SimpleEntityPage<LeaderBoardAsUserEntryDto>(
    "user-toplist",
    LeaderBoardAsUserEntryDto::class, ::LeaderBoardAsUserEntryDto,
    "Felhasználói toplista", "Felhasználói toplista",
    "Felhasználó helyezése a pontversenyben. " +
            "A pontok 10 percenként számítódnak újra. " +
            "Manuális újrageneráláshoz van egy gomb a lap alján.",

    transactionManager,
    { leaderBoardService.getBoardAnywaysForUsers() },

    permission = StaffPermissions.PERMISSION_SHOW_LEADERBOARD,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "leaderboard",
    adminMenuPriority = 1,

    buttonActions = mutableListOf(
        ButtonAction(
            "Újraszámol",
            "refresh",
            ControlPermissions.PERMISSION_CONTROL_LEADERBOARD,
            500,
            "refresh",
            true
        ),
        ButtonAction(
            "Mentés",
            "save/csv",
            StaffPermissions.PERMISSION_SHOW_LEADERBOARD,
            600,
            "save",
            false
        )
    )
) {

    private val refreshPermission = PERMISSION_CONTROL_LEADERBOARD

    @GetMapping("/refresh")
    fun refreshUserTopList(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        if (refreshPermission.validate(user).not()) {
            model.addAttribute("permission", refreshPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /$view/refresh",
                refreshPermission.permissionString)
            return "admin403"
        }

        leaderBoardService.forceRecalculateForUsers()
        return "redirect:/admin/control/${view}"
    }

    @ResponseBody
    @GetMapping("/save/csv", produces = [ MediaType.APPLICATION_OCTET_STREAM_VALUE ])
    fun saveGroupAsCsv(auth: Authentication, response: HttpServletResponse): ByteArray {
        val user = auth.getUser()
        if (!showPermission.validate(user)) {
            throw IllegalStateException("Insufficient permissions")
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"group-toplist-export.csv\"")
        return descriptor.exportToCsv(leaderBoardService.getBoardAnywaysForUsers()).toByteArray()
    }

}
