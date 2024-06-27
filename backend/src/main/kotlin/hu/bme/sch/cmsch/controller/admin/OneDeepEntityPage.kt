package hu.bme.sch.cmsch.controller.admin

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.admin.*
import hu.bme.sch.cmsch.component.ComponentBase
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.model.IdentifiableEntity
import hu.bme.sch.cmsch.model.ManagedEntity
import hu.bme.sch.cmsch.repository.EntityPageDataSource
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.service.ImplicitPermissions.PERMISSION_NOBODY
import hu.bme.sch.cmsch.util.getUser
import hu.bme.sch.cmsch.util.transaction
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.lang.reflect.Method
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

data class ControlAction(
    val name: String,
    val endpoint: String,
    val icon: String,

    @get:JsonIgnore
    val permission: PermissionValidator,

    val order: Int,
    val newPage: Boolean = false,
    var usageString: String = "",
    var basic: Boolean = false
)

data class ButtonAction(
    val name: String,
    val target: String,

    @get:JsonIgnore
    val permission: PermissionValidator,
    val order: Int,
    val icon: String,
    val primary: Boolean = false,
    val newPage: Boolean = false
)

data class SearchSettings(
    val rows: List<String>,
    val displayNames: List<String>,
    val fuzzy: Boolean,
)

inline fun <reified T> calculateSearchSettings(fuzzy: Boolean): SearchSettings {
    return SearchSettings(
        rows = T::class.java.declaredMethods
            .filter { field -> filterForSearchableFields(field) }
            .map { field -> field.name
                .removePrefix("get")
                .removeSuffix("\$annotations")
                .replaceFirstChar { it.lowercase(Locale.getDefault()) }
            },
        displayNames = T::class.java.declaredMethods
            .filter { field -> filterForSearchableFields(field) }
            .map { field -> field.getAnnotationsByType(GenerateOverview::class.java).firstOrNull()?.columnName ?: "" },
        fuzzy = fuzzy)
}

fun filterForSearchableFields(field: Method): Boolean {
    val overviewAnnotation = field.getAnnotationsByType(GenerateOverview::class.java).firstOrNull()
        ?: return false
    return overviewAnnotation.visible
            && overviewAnnotation.useForSearch
            && (overviewAnnotation.renderer == OVERVIEW_TYPE_TEXT
                || overviewAnnotation.renderer == OVERVIEW_TYPE_NUMBER
            )
}

const val INVALID_ID_ERROR = "Object with this id was not found in the database"

open class OneDeepEntityPage<T : IdentifiableEntity>(
    internal val view: String,
    internal val classType: KClass<T>,
    private val supplier: Supplier<T>,
    internal val titleSingular: String,
    internal val titlePlural: String,
    internal val description: String,

    internal var transactionManager: PlatformTransactionManager,
    internal val dataSource: EntityPageDataSource<T, Int>,
    internal val importService: ImportService,
    internal val adminMenuService: AdminMenuService,
    internal val storageService: StorageService,
    internal val component: ComponentBase,
    internal val auditLog: AuditLogService,
    internal val objectMapper: ObjectMapper,
    private val env: Environment,
    internal val entitySourceMapping: Map<String, (T?) -> List<String>> =
        mapOf(Nothing::class.simpleName!! to { listOf() }),

    internal val showPermission: PermissionValidator,
    internal val createPermission: PermissionValidator,
    internal val editPermission: PermissionValidator,
    internal val deletePermission: PermissionValidator,

    internal val showEnabled: Boolean = true,
    internal val createEnabled: Boolean = false,
    internal val editEnabled: Boolean = false,
    internal val deleteEnabled: Boolean = false,
    internal val importEnabled: Boolean = true,
    internal val exportEnabled: Boolean = true,
    internal val duplicateEnabled: Boolean = createEnabled,

    private val adminMenuCategory: String? = null,
    private val adminMenuIcon: String = "check_box_outline_blank",
    private val adminMenuPriority: Int = 1,
    private var ignoreFromMenu: Boolean = false,

    internal val controlActions: MutableList<ControlAction> = mutableListOf(),
    internal val buttonActions: MutableList<ButtonAction> = mutableListOf(),
    internal val searchSettings: SearchSettings? = null
) {

    private val log = LoggerFactory.getLogger(javaClass)
    internal val descriptor = OverviewBuilder(classType)

    @PostConstruct
    fun init() {
        if (!ignoreFromMenu) {
            val category = adminMenuCategory ?: component.javaClass.simpleName
            adminMenuService.registerEntry(
                category, AdminMenuEntry(
                    titlePlural,
                    adminMenuIcon,
                    "/admin/control/${view}",
                    adminMenuPriority,
                    showPermission
                )
            )
        }

        when {
            importEnabled && !exportEnabled -> {
                buttonActions.add(ButtonAction(
                    "Import",
                    "resource",
                    createPermission,
                    200,
                    "upload_file",
                    false
                ))
            }
            !importEnabled && exportEnabled -> {
                buttonActions.add(ButtonAction(
                    "Export",
                    "resource",
                    showPermission,
                    200,
                    "upload_file",
                    false
                ))
            }
            importEnabled -> {
                buttonActions.add(ButtonAction(
                    "Import / Export",
                    "resource",
                    createPermission,
                    200,
                    "upload_file",
                    false
                ))
            }
        }
        if (createEnabled) {
            buttonActions.add(ButtonAction(
                "Új $titleSingular",
                "create",
                createPermission,
                100,
                "add_box",
                true
            ))
            if (duplicateEnabled) {
                controlActions.add(ControlAction(
                    "Másolat készítése",
                    "duplicate/{id}",
                    "content_copy",
                    createPermission,
                    150,
                    usageString = "Új bejegyzés létrehozása meglévő alapján",
                    basic = true
                ))
            }
        }

        if (editEnabled) {
            controlActions.add(ControlAction(
                "Szerkesztés",
                "edit/{id}",
                "edit",
                editPermission,
                100,
                usageString = "Bejegyzés szerkesztése",
                basic = true
            ))
        }
        if (showEnabled) {
            controlActions.add(ControlAction(
                "Megtekintés",
                "show/{id}",
                "visibility",
                showPermission,
                50,
                usageString = "Bejegyzés megnyitása",
                basic = true
            ))
        }

        if (deleteEnabled) {
            controlActions.add(ControlAction(
                "Törlés",
                "delete/{id}",
                "delete",
                deletePermission,
                200,
                usageString = "Bejegyzés törlése",
                basic = true
            ))
            buttonActions.add(ButtonAction(
                "Összes törlése",
                "purge",
                deletePermission,
                300,
                "delete_forever",
                false
            ))
        }

        val instance = supplier.get()
        if (instance is ManagedEntity) {
            val config = instance.getEntityConfig(env)
            if (config != null) {
                buttonActions.add(ButtonAction(
                    "Típus adatok",
                    "entity/${config.name}",
                    showPermission,
                    400,
                    "database",
                    false
                ))
            }
        }

        controlActions.sortBy { it.order }
        buttonActions.sortBy { it.order }
    }

    @GetMapping("")
    open fun view(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (showPermission.validate(user).not()) {
            model.addAttribute("permission", showPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}", showPermission.permissionString)
            return "admin403"
        }

        model.addAttribute("title", titlePlural)
        model.addAttribute("titleSingular", titleSingular)
        model.addAttribute("description", description)
        model.addAttribute("view", view)

        model.addAttribute("columnData", descriptor.getColumns())
        val overview = transactionManager.transaction(readOnly = true) { fetchOverview(user) }
        model.addAttribute("tableData", descriptor.getTableData(filterOverview(user, overview)))

        model.addAttribute("user", user)
        model.addAttribute("controlActions", controlActions.filter { it.permission.validate(user) })
        model.addAttribute("allControlActions", controlActions)
        model.addAttribute("buttonActions", buttonActions.filter { it.permission.validate(user) })
        model.addAttribute("searchSettings", searchSettings)

        attachPermissionInfo(model)

        return "overview4"
    }

    internal fun attachPermissionInfo(model: Model) {
        if (showEnabled && showPermission != PERMISSION_NOBODY)
            model.addAttribute("permissionShow", showPermission.permissionString)

        if (editEnabled && editPermission != PERMISSION_NOBODY)
            model.addAttribute("permissionEdit", editPermission.permissionString)

        if (createEnabled && createPermission != PERMISSION_NOBODY)
            model.addAttribute("permissionCreate", createPermission.permissionString)

        if (deleteEnabled && deletePermission != PERMISSION_NOBODY)
            model.addAttribute("permissionDelete", deletePermission.permissionString)

        model.addAttribute("importEnabled", importEnabled)
        model.addAttribute("exportEnabled", exportEnabled)
    }

    @GetMapping("/edit/{id}")
    fun edit(@PathVariable id: Int, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (editPermission.validate(user).not()) {
            model.addAttribute("permission", editPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/edit/$id", showPermission.permissionString)
            return "admin403"
        }

        if (!editEnabled)
            return "redirect:/admin/control/$view/"

        val entity = transactionManager.transaction(readOnly = true) { dataSource.findById(id) }
        if (entity.isEmpty) {
            model.addAttribute("error", INVALID_ID_ERROR)
        } else {
            val actualEntity = onPreEdit(entity.orElseThrow())
            model.addAttribute("data", actualEntity)
            if (!editPermissionCheck(user, actualEntity)) {
                model.addAttribute("user", user)
                auditLog.admin403(user, component.component, "GET /${view}/edit/$id",
                    "editPermissionCheck() validation")
                return "admin403"
            }
        }

        model.addAttribute("title", titleSingular)
        model.addAttribute("editMode", true)
        model.addAttribute("duplicateMode", false)
        model.addAttribute("view", view)
        model.addAttribute("id", id)
        model.addAttribute("inputs", descriptor.getInputs())
        model.addAttribute("mappings", entitySourceMapping)
        model.addAttribute("user", user)
        model.addAttribute("readOnly", false)
        model.addAttribute("entityMode", false)

        onDetailsView(user, model)
        return "details"
    }

    @GetMapping("/show/{id}")
    fun show(@PathVariable id: Int, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (showPermission.validate(user).not()) {
            model.addAttribute("permission", showPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/show/$id", showPermission.permissionString)
            return "admin403"
        }

        val entity = transactionManager.transaction(readOnly = true) { dataSource.findById(id) }
        if (entity.isEmpty) {
            model.addAttribute("error", INVALID_ID_ERROR)
        } else {
            model.addAttribute("data", entity.orElseThrow())
        }

        model.addAttribute("title", titleSingular)
        model.addAttribute("editMode", true)
        model.addAttribute("duplicateMode", false)
        model.addAttribute("view", view)
        model.addAttribute("id", id)
        model.addAttribute("inputs", descriptor.getInputs())
        model.addAttribute("mappings", entitySourceMapping)
        model.addAttribute("user", user)
        model.addAttribute("readOnly", true)
        model.addAttribute("entityMode", false)

        onDetailsView(user, model)
        return "details"
    }

    @GetMapping("/create")
    fun create(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (createPermission.validate(user).not()) {
            model.addAttribute("permission", createPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/create", createPermission.permissionString)
            return "admin403"
        }

        if (createEnabled.not())
            return "redirect:/admin/control/$view/"

        model.addAttribute("title", titleSingular)
        model.addAttribute("editMode", false)
        model.addAttribute("duplicateMode", false)
        model.addAttribute("view", view)
        model.addAttribute("inputs", descriptor.getInputs())
        model.addAttribute("mappings", entitySourceMapping)
        model.addAttribute("data", null)
        model.addAttribute("user", user)
        model.addAttribute("readOnly", false)
        model.addAttribute("entityMode", false)

        onDetailsView(user, model)
        return "details"
    }

    @GetMapping("/duplicate/{id}")
    fun duplicate(model: Model, auth: Authentication, @PathVariable id: Int): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (createPermission.validate(user).not() && showPermission.validate(user).not()) {
            model.addAttribute("permission", createPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/duplicate/${id}", createPermission.permissionString)
            return "admin403"
        }

        if (duplicateEnabled.not())
            return "redirect:/admin/control/$view/"

        val entity = transactionManager.transaction(readOnly = true) { dataSource.findById(id) }
        if (entity.isEmpty) {
            model.addAttribute("error", INVALID_ID_ERROR)
        } else {
            model.addAttribute("data", entity.orElseThrow())
        }

        model.addAttribute("title", titleSingular)
        model.addAttribute("editMode", false)
        model.addAttribute("duplicateMode", true)
        model.addAttribute("view", view)
        model.addAttribute("inputs", descriptor.getInputs())
        model.addAttribute("mappings", entitySourceMapping)
        model.addAttribute("user", user)
        model.addAttribute("readOnly", false)
        model.addAttribute("entityMode", false)

        onDetailsView(user, model)
        return "details"
    }

    @GetMapping("/delete/{id}")
    fun deleteConfirm(@PathVariable id: Int, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (deletePermission.validate(user).not()) {
            model.addAttribute("permission", deletePermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/delete/$id",
                deletePermission.permissionString)
            return "admin403"
        }

        if (!deleteEnabled)
            return "redirect:/admin/control/$view"

        model.addAttribute("title", titleSingular)
        model.addAttribute("view", view)
        model.addAttribute("id", id)
        model.addAttribute("user", user)

        val entity = transactionManager.transaction(readOnly = true) { dataSource.findById(id) }
        if (entity.isEmpty) {
            model.addAttribute("error", INVALID_ID_ERROR)
        } else {
            val actualEntity = entity.orElseThrow()
            model.addAttribute("item", actualEntity.toString())
            if (!editPermissionCheck(user, actualEntity)) {
                model.addAttribute("user", user)
                auditLog.admin403(user, component.component, "GET /${view}/delete/$id",
                    "editPermissionCheck() validation")
                return "admin403"
            }
        }
        return "delete"
    }

    @PostMapping("/delete/{id}")
    fun delete(@PathVariable id: Int, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        if (deletePermission.validate(user).not()) {
            model.addAttribute("permission", deletePermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "POST /${view}/delete/$id", deletePermission.permissionString)
            return "admin403"
        }

        transactionManager.transaction(readOnly = false, isolation = TransactionDefinition.ISOLATION_REPEATABLE_READ) {
            val entity = dataSource.findById(id).orElseThrow()
            if (!editPermissionCheck(user, entity)) {
                model.addAttribute("user", user)
                auditLog.admin403(
                    user,
                    component.component,
                    "POST /${view}/delete/$id",
                    "editPermissionCheck() validation"
                )
                return "admin403"
            }

            if (!deleteEnabled)
                return "redirect:/admin/control/$view"

            auditLog.delete(user, component.component, "delete ${entity.id} $entity")
            dataSource.delete(entity)
            onEntityDeleted(entity)
        }
        return "redirect:/admin/control/$view"
    }

    @GetMapping("/purge")
    fun purge(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (!deleteEnabled || deletePermission.validate(user).not()) {
            model.addAttribute("permission", deletePermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/purge", deletePermission.permissionString)
            return "admin403"
        }

        model.addAttribute("title", titlePlural)
        model.addAttribute("view", view)
        model.addAttribute("user", user)
        return "purge"
    }

    @PostMapping("/purge")
    fun purgeConfirmed(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        if (!deleteEnabled || deletePermission.validate(user).not()) {
            log.info("User '{}'#{} wanted to purge view '{}'", user.userName, user.id, view)
            throw IllegalStateException("Insufficient permissions")
        }

        log.info("Purging view '{}' by user '{}'#{}", view, user.userName, user.id)
        var before = 0L
        var after = 0L
        transactionManager.transaction(readOnly = false, isolation = TransactionDefinition.ISOLATION_SERIALIZABLE) {
            before = dataSource.count()
            try {
                purgeAllEntities(user)
            } catch (e: Exception) {
                auditLog.delete(user, component.component, "purge all in $view failed: ${e.message}")
                log.error("Purging failed on view '{}'", view, e)
            }
            after = dataSource.count()
        }
        model.addAttribute("purgedCount", before - after)
        log.info("Purged {} on view '{}'", before - after, view)
        auditLog.delete(user, component.component, "purge all in $view (affected: ${before - after})")

        onEntitiesPurged()

        model.addAttribute("title", titlePlural)
        model.addAttribute("view", view)
        model.addAttribute("user", user)
        adminMenuService.addPartsForMenu(user, model)
        return "purge"
    }

    @PostMapping("/create")
    fun create(@ModelAttribute(binding = false) dto: T,
               @RequestParam(required = false) file0: MultipartFile?,
               @RequestParam(required = false) file1: MultipartFile?,
               model: Model,
               auth: Authentication,
    ): String {
        val user = auth.getUser()
        if (createPermission.validate(user).not()) {
            model.addAttribute("permission", createPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "POST /${view}/create", createPermission.permissionString)
            return "admin403"
        }

        if (!createEnabled)
            return "redirect:/admin/control/$view"

        val entity = supplier.get()
        val newValues = StringBuilder("entity new value: ")
        updateEntity(descriptor, user, entity, dto, newValues, false, file0, false, file1)
        entity.id = 0
        if (onEntityPreSave(entity, auth)) {
            auditLog.create(user, component.component, newValues.toString())
            transactionManager.transaction(readOnly = false, isolation = TransactionDefinition.ISOLATION_READ_COMMITTED) {
                dataSource.save(entity)
            }
        }
        onEntityChanged(entity)
        return "redirect:/admin/control/$view"
    }

    @PostMapping("/edit/{id}")
    fun edit(@PathVariable id: Int,
             @ModelAttribute(binding = false) dto: T,
             @RequestParam(required = false) file0: MultipartFile?,
             @RequestParam(required = false) file1: MultipartFile?,
             model: Model,
             auth: Authentication,
             @RequestParam(defaultValue = "false") delete0: Boolean,
             @RequestParam(defaultValue = "false") delete1: Boolean,
    ): String {
        val user = auth.getUser()
        if (editPermission.validate(user).not()) {
            model.addAttribute("permission", editPermission.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "POST /${view}/edit/${id}", editPermission.permissionString)
            return "admin403"
        }

        val entity = transactionManager.transaction(readOnly = true) { dataSource.findById(id) }
        if (entity.isEmpty) {
            return "redirect:/admin/control/$view/edit/$id"
        }
        val actualEntity = entity.orElseThrow()
        if (!editPermissionCheck(user, actualEntity)) {
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "POST /${view}/edit/${id}", "editPermissionCheck() validation")
            return "admin403"
        }

        if (!editEnabled)
            return "redirect:/admin/control/$view/"

        val newValues = StringBuilder("entity new value: ")
        updateEntity(descriptor, user, actualEntity, dto, newValues, delete0, file0, delete1, file1)
        actualEntity.id = id
        if (onEntityPreSave(actualEntity, auth)) {
            auditLog.edit(user, component.component, newValues.toString())
            transactionManager.transaction(readOnly = false, isolation = TransactionDefinition.ISOLATION_SERIALIZABLE) {
                dataSource.save(actualEntity)
            }
        }
        onEntityChanged(actualEntity)
        return "redirect:/admin/control/$view"
    }

    private val sqlDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    internal fun updateEntity(
        descriptor: OverviewBuilder<T>, user: CmschUser, entity: T, dto: T, newValues: StringBuilder,
        delete0: Boolean, file0: MultipartFile?,
        delete1: Boolean, file1: MultipartFile?
    ) {
        descriptor.getInputs().forEach {
            if (it.first is KMutableProperty1<out Any, *> && !it.second.ignore && it.second.minimumRole.value <= user.role.value) {
                when {
                    it.second.interpreter == INTERPRETER_INHERIT && it.second.type == INPUT_TYPE_FILE -> {
                        when (it.second.fileId) {
                            "0" -> {
                                if (delete0) {
                                    (it.first as KMutableProperty1<out Any, *>).setter.call(entity, "")
                                } else {
                                    file0?.let { file -> storageService.saveObject(view, file) }?.ifPresent { file ->
                                        (it.first as KMutableProperty1<out Any, *>).setter.call(entity, "$view/$file")
                                        newValues.append(it.first.name).append("=name@").append(view)
                                            .append("/").append(file).append(", ")
                                    }
                                }
                            }
                            "1" -> {
                                if (delete1) {
                                    (it.first as KMutableProperty1<out Any, *>).setter.call(entity, "")
                                } else {
                                    file1?.let { file -> storageService.saveObject(view, file) }?.ifPresent { file ->
                                        newValues.append(it.first.name).append("=name@").append(view)
                                            .append("/").append(file).append(", ")
                                        (it.first as KMutableProperty1<out Any, *>).setter.call(entity, "$view/$file")
                                    }
                                }
                            }
                            else -> {
                                log.error("Invalid file field name: file${it.second.fileId}")
                            }
                        }
                    }
                    (it.second.interpreter == INTERPRETER_INHERIT || it.second.interpreter == INTERPRETER_SEARCH) && it.second.type != INPUT_TYPE_FILE -> {
                        (it.first as KMutableProperty1<out Any, *>).setter.call(entity, it.first.getter.call(dto))
                        newValues.append(it.first.name).append("=").append(it.first.getter.call(dto)?.toString()
                            ?.replace("\r", "")?.replace("\n", "") ?: "<null>").append(", ")
                    }
                    it.second.interpreter == INTERPRETER_PATH -> {
                        val value = it.first.getter.call(dto)
                            ?.toString()
                            ?.lowercase()
                            ?.replace(" ", "-")
                            ?.replace(Regex("[^a-z0-9-.]"), "") ?: "<null>"
                        (it.first as KMutableProperty1<out Any, *>).setter.call(entity, value)
                        newValues.append(it.first.name).append("=").append(value).append(", ")
                    }
                    it.second.interpreter == INTERPRETER_CUSTOM -> {
                        handleCustomInterpreter(it, newValues)
                    }
                }
            }
        }
    }

    @GetMapping("/resource")
    fun resource(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (importEnabled) {
            if (createPermission.validate(user).not() || showPermission.validate(user).not()) {
                model.addAttribute("permission", createPermission.permissionString)
                model.addAttribute("user", user)
                auditLog.admin403(user, component.component, "GET /${view}/resource", createPermission.permissionString)
                return "admin403"
            }
        } else if (exportEnabled) {
            if (showPermission.validate(user).not()) {
                model.addAttribute("permission", showPermission.permissionString)
                model.addAttribute("user", user)
                auditLog.admin403(user, component.component, "GET /${view}/resource", showPermission.permissionString)
                return "admin403"
            }
        } else {
            model.addAttribute("permission", PERMISSION_NOBODY.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, component.component, "GET /${view}/resource", PERMISSION_NOBODY.permissionString)
            return "admin403"
        }

        model.addAttribute("title", titlePlural)
        model.addAttribute("view", view)
        model.addAttribute("user", user)
        model.addAttribute("importEnabled", importEnabled && createEnabled && createPermission.validate(user))
        model.addAttribute("exportEnabled", exportEnabled && showPermission.validate(user))
        return "resource"
    }

    @ResponseBody
    @GetMapping("/export/csv", produces = [ MediaType.APPLICATION_OCTET_STREAM_VALUE ])
    fun export(auth: Authentication, response: HttpServletResponse): ByteArray {
        val user = auth.getUser()
        if (!exportEnabled || !showPermission.validate(user)) {
            throw IllegalStateException("Insufficient permissions")
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"$view-export.csv\"")
        val overview = transactionManager.transaction(readOnly = true) { fetchOverviewForExport(user) }
        return descriptor.exportToCsv(filterOverview(user, overview).toList()).toByteArray()
    }

    @PostMapping("/import/csv")
    fun import(file: MultipartFile?, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        if (!importEnabled || !createEnabled || !createPermission.validate(user)) {
            throw IllegalStateException("Insufficient permissions")
        }

        log.info("Importing into {}", view)
        var before = 0L
        var after = 0L
        transactionManager.transaction(readOnly = false, isolation = TransactionDefinition.ISOLATION_SERIALIZABLE) {
            before = dataSource.count()
            file?.inputStream?.let { stream ->
                importService.importEntities(dataSource, stream, classType)
            }
            after = dataSource.count()
        }
        onImported()
        model.addAttribute("importedCount", after - before)
        val action = "imported $view (new entities: ${after - before})"
        auditLog.create(user, component.component, action)
        log.info(action)

        model.addAttribute("title", titlePlural)
        model.addAttribute("view", view)
        model.addAttribute("user", user)
        model.addAttribute("importEnabled", importEnabled && createEnabled && createPermission.validate(user))
        model.addAttribute("exportEnabled", exportEnabled && showPermission.validate(user))
        adminMenuService.addPartsForMenu(user, model)
        return "resource"
    }

    @GetMapping("/entity/{entity}")
    fun redirectEntity(@PathVariable entity: String): String {
        return "redirect:/admin/control/entity/${entity}"
    }

    open fun onEntityChanged(entity: T) {
        // Overridden when notification is required
    }

    open fun onEntityDeleted(entity: T) {
        // Overridden when notification is required
    }

    open fun onEntityPreSave(entity: T, auth: Authentication): Boolean {
        // Overridden when notification is required
        return true
    }

    open fun onDetailsView(entity: CmschUser, model: Model) {
        // Overridden when notification is required
    }

    open fun onPreEdit(actualEntity: T): T {
        // Overridden when notification is required
        return actualEntity
    }

    open fun onImported() {
        // Overridden when notification is required
    }

    open fun filterOverview(user: CmschUser, rows: Iterable<T>): Iterable<T> {
        return rows
    }

    open fun editPermissionCheck(user: CmschUser, entity: T): Boolean {
        return true
    }

    open fun purgeAllEntities(user: CmschUser) {
        dataSource.deleteAll()
    }

    open fun fetchOverview(user: CmschUser): Iterable<T> {
        return dataSource.findAll()
    }

    open fun fetchOverviewForExport(user: CmschUser): Iterable<T> {
        return dataSource.findAll()
    }

    open fun handleCustomInterpreter(it: Pair<KProperty1<out Any, *>, GenerateInput>, newValues: StringBuilder) {
        TODO("Custom interpreter was found but handleCustomInterpreter() was not overridden")
    }

    open fun onEntitiesPurged() {
        // Overridden when notification is required
    }

}
