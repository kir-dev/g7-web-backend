package hu.bme.sch.cmsch.component.staticpage

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
@RequestMapping("/admin/control/static-pages")
@ConditionalOnBean(StaticPageComponent::class)
class StaticPageController(
    repo: StaticPageRepository,
    importService: ImportService,
    adminMenuService: AdminMenuService,
    component: StaticPageComponent,
    auditLog: AuditLogService,
    objectMapper: ObjectMapper,
    transactionManager: PlatformTransactionManager,
    env: Environment,
    storageService: StorageService
) : OneDeepEntityPage<StaticPageEntity>(
    "static-pages",
    StaticPageEntity::class, ::StaticPageEntity,
    "Statikus Oldal", "Statikus oldalak",
    "A statikus oldalak segítségével bármilyen tartalom megjeleníthető az oldalon, ami nem igényel szerver " +
            "semmilyen logikát vagy automatizmust a szerver felől. Az oldalak tartalmazhatnak képeket, szövegeket és " +
            "táblázatokat is, ezáltal kiválóan alkalmas gyakori kérdések, kapcsolat menü vagy például program " +
            "összefoglaló lap megvalósítására.",

    transactionManager,
    repo,
    importService,
    adminMenuService,
    storageService,
    component,
    auditLog,
    objectMapper,
    env,

    showPermission =   StaffPermissions.PERMISSION_SHOW_STATIC_PAGES,
    createPermission = StaffPermissions.PERMISSION_CREATE_STATIC_PAGES,
    editPermission =   StaffPermissions.PERMISSION_EDIT_STATIC_PAGES,
    deletePermission = StaffPermissions.PERMISSION_DELETE_STATIC_PAGES,

    createEnabled = true,
    editEnabled = true,
    deleteEnabled = true,
    importEnabled = true,
    exportEnabled = true,

    adminMenuIcon = "article",
    adminMenuPriority = 1,

    searchSettings = calculateSearchSettings<StaticPageEntity>(false)
) {

    override fun filterOverview(user: CmschUser, rows: Iterable<StaticPageEntity>): Iterable<StaticPageEntity> {
        return rows.filter { editPermissionCheck(user, it) }
    }

    override fun editPermissionCheck(user: CmschUser, entity: StaticPageEntity): Boolean {
        return user.isAdmin() || entity.permissionToEdit.isBlank() || user.hasPermission(entity.permissionToEdit)
                || StaffPermissions.PERMISSION_MODIFY_ANY_STATIC_PAGES.validate(user)
    }

    override fun purgeAllEntities(user: CmschUser) {
        dataSource.findAll()
            .filter { editPermissionCheck(user, it) }
            .forEach { dataSource.delete(it) }
    }

}
