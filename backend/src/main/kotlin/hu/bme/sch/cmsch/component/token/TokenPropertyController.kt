package hu.bme.sch.cmsch.component.token

import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.controller.admin.OneDeepEntityPage
import hu.bme.sch.cmsch.controller.admin.calculateSearchSettings
import hu.bme.sch.cmsch.repository.ManualRepository
import hu.bme.sch.cmsch.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@Controller
@RequestMapping("/admin/control/raw-token-properties")
@ConditionalOnBean(TokenComponent::class)
class TokenSubmissionsController(
    private val repo: TokenPropertyRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: TokenComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<TokenPropertyRawView>(
    "raw-token-properties",
    TokenPropertyRawView::class, ::TokenPropertyRawView,
    "Nyers beolvasás", "Nyers beolvasások",
    "Nyers token beolvasások",

    transactionManager,
    object : ManualRepository<TokenPropertyRawView, Int>() {
        override fun findAll(): Iterable<TokenPropertyRawView> {
            return repo.findAll().map { mapTokenProperty(it) }
        }

        override fun count(): Long {
            return repo.count()
        }

        override fun deleteAll() {
            repo.deleteAll()
        }

        override fun delete(entity: TokenPropertyRawView) {
            repo.findById(entity.id).ifPresent {
                repo.delete(it)
            }
        }

        override fun findById(id: Int): Optional<TokenPropertyRawView> {
            return repo.findById(id).map { mapTokenProperty(it) }
        }

        private fun mapTokenProperty(it: TokenPropertyEntity) = TokenPropertyRawView(
            it.id,
            it.ownerUser?.id ?: 0,
            it.ownerUser?.userName ?: "",
            it.ownerGroup?.id ?: 0,
            it.ownerGroup?.name ?: "",
            it.token?.score ?: 0,
            it.token?.title ?: "no-token",
            it.recieved
        )

    },

    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission = ControlPermissions.PERMISSION_CONTROL_TOKEN,
    createPermission = ImplicitPermissions.PERMISSION_NOBODY,
    editPermission =   ImplicitPermissions.PERMISSION_NOBODY,
    deletePermission = ControlPermissions.PERMISSION_CONTROL_TOKEN,

    createEnabled = false,
    editEnabled   = false,
    deleteEnabled = true,
    importEnabled = false,
    exportEnabled = true,

    adminMenuIcon = "raw_on",
    adminMenuPriority = 8,

    searchSettings = calculateSearchSettings<TokenPropertyEntity>(false)
)
