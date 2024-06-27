package hu.bme.sch.cmsch.component.debt

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.getUser
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/debts")
@ConditionalOnBean(DebtComponent::class)
class SoldProductController(
    repo: SoldProductRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: DebtComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    private val clock: TimeService,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<SoldProductEntity>(
    "debts",
    SoldProductEntity::class, ::SoldProductEntity,
    "Tranzakció", "Tranzakciók",
    "Az összes eladásból származó tranzakciók.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_DEBTS,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission =   StaffPermissions.PERMISSION_EDIT_DEBTS,
    deletePermission = StaffPermissions.PERMISSION_DELETE_DEBTS,

    createEnabled = false,
    editEnabled   = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "sync_alt",
    adminMenuPriority = 2,

    searchSettings = calculateSearchSettings<SoldProductEntity>(false)
) {

    override fun onEntityPreSave(entity: SoldProductEntity, auth: Authentication): Boolean {
        val date = clock.getTimeInSeconds()
        val user = auth.getUser()
        entity.log = "${entity.log} '${user.userName}'(${user.id}) changed [shipped: ${entity.shipped}, payed: ${entity.payed}, finished: ${entity.finsihed}] at $date;"
        return true
    }

}
