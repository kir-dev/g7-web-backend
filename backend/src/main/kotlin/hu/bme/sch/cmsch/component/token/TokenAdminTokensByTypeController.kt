package hu.bme.sch.cmsch.component.token

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.TwoDeepEntityPage
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/control/token-properties-type")
@ConditionalOnBean(TokenComponent::class)
class TokenAdminTokensByTypeController(
    private val repo: TokenPropertyRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: TokenComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : TwoDeepEntityPage<TokenStatVirtualEntity, TokenPropertyVirtualEntity>(
    "token-properties-type",
    TokenStatVirtualEntity::class,
    TokenPropertyVirtualEntity::class, ::TokenPropertyVirtualEntity,
    "Token statisztika", "Token statisztika",
    "Ki szerezte be melyik tokent",

    transactionManager,
    object : ManualRepository<TokenStatVirtualEntity, Int>() {
        override fun findAll(): Iterable<TokenStatVirtualEntity> {
            return repo.findAll()
                .groupBy { it.token?.id }
                .map { it.value }
                .filter { it.isNotEmpty() }
                .map { tokenProperty ->
                    TokenStatVirtualEntity(
                        tokenProperty[0].token?.id ?: 0,
                        tokenProperty[0].token?.title ?: "n/a",
                        tokenProperty[0].token?.type ?: "n/a",
                        tokenProperty.count()
                    )
                }
        }

        override fun delete(entity: TokenStatVirtualEntity) {
            repo.deleteById(entity.id)
        }

        override fun deleteAll() {
            repo.deleteAll()
        }
    },
    object : ManualRepository<TokenPropertyVirtualEntity, Int>() {
        override fun delete(entity: TokenPropertyVirtualEntity) {
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

    adminMenuIcon = "analytics",
    adminMenuPriority = 6,
) {

    override fun fetchSublist(id: Int): Iterable<TokenPropertyVirtualEntity> {
        return repo.findAllByToken_Id(id)
            .map {
                TokenPropertyVirtualEntity(
                    it.id,
                    it.ownerUser?.fullName ?: it.ownerGroup?.name ?: "n/a",
                    it.recieved
                )
            }
    }

}
