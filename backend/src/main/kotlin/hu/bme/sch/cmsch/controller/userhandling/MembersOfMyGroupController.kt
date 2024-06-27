package hu.bme.sch.cmsch.controller.userhandling

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.component.app.UserHandlingComponent
import hu.bme.sch.cmsch.controller.admin.SimpleEntityPage
import hu.bme.sch.cmsch.dto.virtual.GroupMemberVirtualEntity
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.ImplicitPermissions.PERMISSION_IMPLICIT_HAS_GROUP
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/members-of-my-group")
@ConditionalOnBean(UserHandlingComponent::class)
class MembersOfMyGroupController(
    private val userService: UserService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: UserHandlingComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : SimpleEntityPage<GroupMemberVirtualEntity>(
    "members-of-my-group",
    GroupMemberVirtualEntity::class, ::GroupMemberVirtualEntity,
    "Csoportom tagjai", "Csoportom tagjai",
    "A csoportodban lévő emberek. Ameddig valaki nem jelentkezik be, addig itt nem látszik, hogy rendező-e.",

    transactionManager,
    { user ->
        userService.allMembersOfGroup(userService.getByUserId(user.id).groupName)
    },

    permission = PERMISSION_IMPLICIT_HAS_GROUP,

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    adminMenuIcon = "group",
    adminMenuPriority = 6,
)
