package hu.bme.sch.cmsch.component.token

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.TwoDeepEntityPage
import hu.bme.sch.cmsch.repository.GroupRepository
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/token-properties-user")
@ConditionalOnBean(TokenComponent::class)
class TokenAdminTokensByUsersController(
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
) : TwoDeepEntityPage<TokenListByUserVirtualEntity, TokenVirtualEntity>(
    "token-properties-user",
    TokenListByUserVirtualEntity::class,
    TokenVirtualEntity::class, ::TokenVirtualEntity,
    "Felhasználói tokenek", "Felhasználói tokenek",
    "Beolvasott tokenek felhasználónként csoportosítva",

    transactionManager,
    object : ManualRepository<TokenListByUserVirtualEntity, Int>() {
        override fun findAll(): Iterable<TokenListByUserVirtualEntity> {
            return repo.findAll()
                .groupBy { it.ownerUser?.id ?: 0 }
                .map { it.value }
                .filter { it.isNotEmpty() }
                .map { tokenProperty ->
                    val groupName = groupRepository.findById(tokenProperty[0].ownerUser?.id ?: 0).map { it.name }.orElse("n/a")
                    TokenListByUserVirtualEntity(
                        tokenProperty[0].ownerUser?.id ?: 0,
                        tokenProperty[0].ownerUser?.fullName ?: "n/a",
                        groupName,
                        tokenProperty.count()
                    )
                }
        }

        override fun delete(entity: TokenListByUserVirtualEntity) {
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
    adminMenuPriority = 2,
) {

    override fun fetchSublist(id: Int): Iterable<TokenVirtualEntity> {
        return repo.findAllByOwnerUser_Id(id)
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
