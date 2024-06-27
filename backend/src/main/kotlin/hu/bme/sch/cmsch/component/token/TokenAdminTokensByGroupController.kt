package hu.bme.sch.cmsch.component.token

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.TwoDeepEntityPage
import hu.bme.sch.cmsch.repository.GroupRepository
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/token-properties-group")
@ConditionalOnBean(TokenComponent::class)
@ConditionalOnProperty(
    prefix = "hu.bme.sch.cmsch.startup",
    name = ["token-ownership-mode"],
    havingValue = "GROUP",
    matchIfMissing = false
)
class TokenAdminTokensByGroupController(
    private val repo: TokenPropertyRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: TokenComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    private val groupRepository: GroupRepository,
    env: Environment,
    storageService: StorageService
) : TwoDeepEntityPage<TokenListByGroupVirtualEntity, TokenVirtualEntity>(
    "token-properties-group",
    TokenListByGroupVirtualEntity::class,
    TokenVirtualEntity::class, ::TokenVirtualEntity,
    "Csoportos tokenek", "Csoportos tokenek",
    "Tokenek csoportonként csoportosítva",

    transactionManager,
    object : ManualRepository<TokenListByGroupVirtualEntity, Int>() {
        override fun findAll(): Iterable<TokenListByGroupVirtualEntity> {
            return repo.findAll()
                .groupBy { it.ownerGroup?.id ?: 0 }
                .map { it.value }
                .filter { it.isNotEmpty() }
                .map { token ->
                    val groupName = groupRepository.findById(token[0].ownerGroup?.id ?: 0).map { it.name }.orElse("n/a")
                    TokenListByGroupVirtualEntity(
                        token[0].ownerGroup?.id ?: 0,
                        groupName,
                        token.count()
                    )
                }
        }

        override fun delete(entity: TokenListByGroupVirtualEntity) {
            repo.deleteById(entity.id)
        }

        override fun deleteAll() {
            repo.deleteAll()
        }
    },
    object : ManualRepository<TokenVirtualEntity, Int>() {
        override fun delete(entity: TokenVirtualEntity) {
            repo.deleteById(entity.id)
        }

        override fun deleteAll() {
            repo.deleteAll()
        }
    },
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_TOKEN_SUBMISSIONS,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission =   ImplicitPermissions.PERMISSION_NOBODY,
    deletePermission = StaffPermissions.PERMISSION_EDIT_TOKEN_SUBMISSIONS,

    createEnabled = false,
    editEnabled   = false,
    deleteEnabled = true,
    importEnabled = false,
    exportEnabled = false,

    adminMenuIcon = "local_activity",
    adminMenuPriority = 3,
) {

    override fun fetchSublist(id: Int): Iterable<TokenVirtualEntity> {
        return repo.findAllByOwnerGroup_Id(id)
            .map {
                TokenVirtualEntity(
                    it.id,
                    it.token?.title ?: "n/a",
                    it.token?.type ?: "n/a",
                    it.token?.score ?: 0,
                    it.recieved
                )
            }
    }

}
