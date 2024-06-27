package hu.bme.sch.cmsch.component.debt

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.TwoDeepEntityPage
import hu.bme.sch.cmsch.repository.GroupRepository
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.ImplicitPermissions.PERMISSION_NOBODY
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_EDIT_DEBTS
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_SHOW_DEBTS
import hu.bme.sch.cmsch.util.getUser
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/control/debts-by-users")
@ConditionalOnBean(DebtComponent::class)
class DebtAdminDebtsByUsersController(
    private val soldProductRepository: SoldProductRepository,
    private val groupRepository: GroupRepository,
    private val clock: TimeService,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: DebtComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : TwoDeepEntityPage<DebtsByUserVirtualEntity, SoldProductEntity>(
    "debts-by-users",
    DebtsByUserVirtualEntity::class,
    SoldProductEntity::class, ::SoldProductEntity,
    "Tartozás", "Felhasználó tartozásai",
    "Tartozások felhasználónként csoportosítva",

    transactionManager,
    object : ManualRepository<DebtsByUserVirtualEntity, Int>() {
        override fun findAll(): Iterable<DebtsByUserVirtualEntity> {
            return soldProductRepository.findAll().groupBy { it.ownerId }
                .map { it.value }
                .filter { it.isNotEmpty() }
                .map { debts ->
                    val groupName = groupRepository.findById(debts[0].responsibleGroupId).map { it.name }.orElse("n/a")
                    DebtsByUserVirtualEntity(
                        debts[0].ownerId,
                        debts[0].ownerName,
                        groupName,
                        debts.sumOf { it.price },
                        debts.filter { !it.payed }.sumOf { it.price },
                        debts.filter { !it.finsihed }.sumOf { it.price }
                    )
                }
        }

    },
    soldProductRepository,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   PERMISSION_SHOW_DEBTS,
    createPermission = PERMISSION_NOBODY,
    editPermission =   PERMISSION_EDIT_DEBTS,
    deletePermission = PERMISSION_NOBODY,

    createEnabled = false,
    editEnabled   = true,
    deleteEnabled = false,
    importEnabled = false,
    exportEnabled = false,

    adminMenuIcon = "account_balance",
    adminMenuPriority = 12,
) {

    override fun fetchSublist(id: Int): Iterable<SoldProductEntity> {
        return soldProductRepository.findAllByOwnerId(id)
    }

    override fun onEntityPreSave(entity: SoldProductEntity, auth: Authentication): Boolean {
        val date = clock.getTimeInSeconds()
        val user = auth.getUser()
        entity.log = "${entity.log} '${user.userName}'(${user.id}) changed [shipped: ${entity.shipped}, payed: ${entity.payed}, finished: ${entity.finsihed}] at $date;"
        return true
    }

}
