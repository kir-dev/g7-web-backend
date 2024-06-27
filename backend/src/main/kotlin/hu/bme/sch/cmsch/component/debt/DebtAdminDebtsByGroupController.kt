package hu.bme.sch.cmsch.component.debt

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.TwoDeepEntityPage
import hu.bme.sch.cmsch.repository.GroupRepository
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.StaffPermissions.PERMISSION_EDIT_DEBTS
import hu.bme.sch.cmsch.util.getUser
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/control/debts-by-group")
@ConditionalOnBean(DebtComponent::class)
class DebtAdminDebtsByGroupController(
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
) : TwoDeepEntityPage<DebtsByGroupVirtualEntity, SoldProductEntity>(
    "debts-by-group",
    DebtsByGroupVirtualEntity::class,
    SoldProductEntity::class, ::SoldProductEntity,
    "Csoport Tartozása", "Csoportok tartozásai",
    "Tartozások csoportonként csoportosítva",

    transactionManager,
    object : ManualRepository<DebtsByGroupVirtualEntity, Int>() {
        override fun findAll(): Iterable<DebtsByGroupVirtualEntity> {
            return soldProductRepository.findAll().groupBy { it.responsibleGroupId }
                .map { it.value }
                .filter { it.isNotEmpty() }
                .map { debts ->
                    val groupName = groupRepository.findById(debts[0].responsibleGroupId).map { it.name }.orElse("n/a")
                    DebtsByGroupVirtualEntity(
                        debts[0].responsibleGroupId,
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

    showPermission =   StaffPermissions.PERMISSION_EDIT_DEBTS,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission =   PERMISSION_EDIT_DEBTS,
    deletePermission = ImplicitPermissions.PERMISSION_NOBODY,

    createEnabled = false,
    editEnabled   = true,
    deleteEnabled = false,
    importEnabled = false,
    exportEnabled = false,

    adminMenuIcon = "account_balance",
    adminMenuPriority = 11,
) {

    override fun fetchSublist(id: Int): Iterable<SoldProductEntity> {
        return soldProductRepository.findAllByResponsibleGroupId(id)
    }

    override fun onEntityPreSave(entity: SoldProductEntity, auth: Authentication): Boolean {
        val date = clock.getTimeInSeconds()
        val user = auth.getUser()
        entity.log = "${entity.log} '${user.userName}'(${user.id}) changed [shipped: ${entity.shipped}, payed: ${entity.payed}, finished: ${entity.finsihed}] at $date;"
        return true
    }


}
