package hu.bme.sch.cmsch.controller.admin

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hu.bme.sch.cmsch.admin.INPUT_TYPE_FILE
import hu.bme.sch.cmsch.admin.INTERPRETER_INHERIT
import hu.bme.sch.cmsch.admin.INTERPRETER_SEARCH
import hu.bme.sch.cmsch.admin.OverviewBuilder
import hu.bme.sch.cmsch.component.ComponentBase
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.controller.INVALID_ID_ERROR
import hu.bme.sch.cmsch.model.IdentifiableEntity
import hu.bme.sch.cmsch.repository.EntityPageDataSource
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.getUser
import hu.bme.sch.cmsch.util.uploadFile
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.function.Supplier
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

data class ControlAction(
    val name: String,
    val endpoint: String,
    val icon: String,
    val permission: PermissionValidator,
    val order: Int
)

data class ButtonAction(
    val name: String,
    val target: String,
    val permission: PermissionValidator,
    val order: Int,
    val icon: String,
    val primary: Boolean = false
)

open class OneDeepEntityPage<T : IdentifiableEntity>(
    internal val view: String,
    classType: KClass<T>,
    private val supplier: Supplier<T>,
    internal val titleSingular: String,
    internal val titlePlural: String,
    internal val description: String,

    private val dataSource: EntityPageDataSource<T, Int>,
    internal val importService: ImportService,
    internal val adminMenuService: AdminMenuService,
    internal val component: ComponentBase,
    private val auditLog: AuditLogService,
    private val objectMapper: ObjectMapper,
    private val entitySourceMapping: Map<String, (T?) -> List<String>> =
        mapOf(Nothing::class.simpleName!! to { listOf() }),

    private val showPermission: PermissionValidator,
    private val createPermission: PermissionValidator,
    private val editPermission: PermissionValidator,
    private val deletePermission: PermissionValidator,

    private val createEnabled: Boolean = false,
    private val editEnabled: Boolean = false,
    private val deleteEnabled: Boolean = false,
    private val importEnabled: Boolean = true,
    private val exportEnabled: Boolean = true,

    private val adminMenuIcon: String = "check_box_outline_blank",
    private val adminMenuPriority: Int = 1,

    private val controlActions: MutableList<ControlAction> = mutableListOf(),
    private val buttonActions: MutableList<ButtonAction> = mutableListOf()
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val descriptor = OverviewBuilder(classType)

    @PostConstruct
    fun init() {
        adminMenuService.registerEntry(component.javaClass.simpleName, AdminMenuEntry(
            titlePlural,
            adminMenuIcon,
            "/admin/control/${view}",
            adminMenuPriority,
            showPermission
        ))

        if (createEnabled) {
            buttonActions.add(ButtonAction(
                "Új $titlePlural",
                "/admin/control/news/create",
                createPermission,
                100,
                "add_box",
                true
            ))
            if (importEnabled || exportEnabled) {
                buttonActions.add(ButtonAction(
                    "Import / Export",
                    "/admin/control/news/resource",
                    createPermission,
                    200,
                    "upload_file",
                    false
                ))
            }
        }

        if (editEnabled) {
            controlActions.add(ControlAction(
                "Szerkesztés",
                "/admin/control/news/edit/{id}",
                "edit",
                editPermission,
                100
            ))
        } else {
            controlActions.add(ControlAction(
                "Megtekintés",
                "/admin/control/news/show/{id}",
                "visibility",
                showPermission,
                100
            ))
        }

        if (deleteEnabled) {
            controlActions.add(ControlAction(
                "Törlés",
                "/admin/control/news/delete/{id}",
                "delete",
                deletePermission,
                200
            ))
            buttonActions.add(ButtonAction(
                "Összes törlése",
                "/admin/control/news/purge",
                deletePermission,
                300,
                "delete_forever",
                false
            ))
        }

        controlActions.sortBy { it.order }
        buttonActions.sortBy { it.order }
    }

    @GetMapping("")
    fun view(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (showPermission.validate(user).not()) {
            model.addAttribute("permission", showPermission.permissionString)
            model.addAttribute("user", user)
            return "admin403"
        }

        model.addAttribute("title", titlePlural)
        model.addAttribute("titleSingular", titleSingular)
        model.addAttribute("description", description)
        model.addAttribute("view", view)

        model.addAttribute("columnData", descriptor.getColumnsAsJson())
        model.addAttribute("tableData", descriptor.getTableDataAsJson(filterOverview(user, fetchOverview())))

        model.addAttribute("user", user)
        model.addAttribute("controlActions", toJson(controlActions.filter { it.permission.validate(user) }))
        model.addAttribute("buttonActions", buttonActions.filter { it.permission.validate(user) })

        return "overview4"
    }

    private fun <ControlAction> toJson(list: List<ControlAction>): String {
        return objectMapper
            .writerFor(object : TypeReference<List<ControlAction>>() {})
            .writeValueAsString(list)
    }

    @GetMapping("/edit/{id}")
    fun edit(@PathVariable id: Int, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (editPermission.validate(user).not()) {
            model.addAttribute("permission", editPermission.permissionString)
            model.addAttribute("user", user)
            return "admin403"
        }

        if (!editEnabled)
            return "redirect:/admin/control/$view/"

        val entity = dataSource.findById(id)
        if (entity.isEmpty) {
            model.addAttribute("error", INVALID_ID_ERROR)
        } else {
            val actualEntity = onPreEdit(entity.orElseThrow())
            model.addAttribute("data", actualEntity)
            if (!editPermissionCheck(user, actualEntity)) {
                model.addAttribute("user", user)
                return "admin403"
            }
        }

        model.addAttribute("title", titleSingular)
        model.addAttribute("editMode", true)
        model.addAttribute("view", view)
        model.addAttribute("id", id)
        model.addAttribute("inputs", descriptor.getInputs())
        model.addAttribute("mappings", entitySourceMapping)
        model.addAttribute("user", user)
        model.addAttribute("readOnly", false)

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
            return "admin403"
        }

        val entity = dataSource.findById(id)
        if (entity.isEmpty) {
            model.addAttribute("error", INVALID_ID_ERROR)
        }
        model.addAttribute("data", entity.orElseThrow())

        model.addAttribute("title", titleSingular)
        model.addAttribute("editMode", true)
        model.addAttribute("view", view)
        model.addAttribute("id", id)
        model.addAttribute("inputs", descriptor.getInputs())
        model.addAttribute("mappings", entitySourceMapping)
        model.addAttribute("user", user)
        model.addAttribute("readOnly", true)

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
            return "admin403"
        }

        if (createEnabled.not())
            return "redirect:/admin/control/$view/"

        model.addAttribute("title", titleSingular)
        model.addAttribute("editMode", false)
        model.addAttribute("view", view)
        model.addAttribute("inputs", descriptor.getInputs())
        model.addAttribute("mappings", entitySourceMapping)
        model.addAttribute("data", null)
        model.addAttribute("user", user)
        model.addAttribute("readOnly", false)

        onDetailsView(user, model)
        return "details4"
    }

    @GetMapping("/delete/{id}")
    fun deleteConfirm(@PathVariable id: Int, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (deletePermission.validate(user).not()) {
            model.addAttribute("permission", deletePermission.permissionString)
            model.addAttribute("user", user)
            return "admin403"
        }

        if (!deleteEnabled)
            return "redirect:/admin/control/$view/"

        model.addAttribute("title", titleSingular)
        model.addAttribute("view", view)
        model.addAttribute("id", id)
        model.addAttribute("user", user)

        val entity = dataSource.findById(id)
        if (entity.isEmpty) {
            model.addAttribute("error", INVALID_ID_ERROR)
        } else {
            val actualEntity = entity.orElseThrow()
            model.addAttribute("item", actualEntity.toString())
            if (!editPermissionCheck(user, actualEntity)) {
                model.addAttribute("user", user)
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
            return "admin403"
        }

        val entity = dataSource.findById(id).orElseThrow()
        if (!editPermissionCheck(user, entity)) {
            model.addAttribute("user", user)
            return "admin403"
        }

        if (!deleteEnabled)
            return "redirect:/admin/control/$view/"

        auditLog.delete(user, component.component, "delete: $entity")
        dataSource.delete(entity)
        onEntityDeleted(entity)
        return "redirect:/admin/control/$view/"
    }

    @GetMapping("/purge")
    fun purge(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (!deleteEnabled || deletePermission.validate(user).not()) {
            model.addAttribute("permission", deletePermission.permissionString)
            model.addAttribute("user", user)
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
        val before = dataSource.count()
        try {
            dataSource.deleteAll()
        } catch (e : Exception) {
            log.error("Purging failed on view '{}'", view, e)
        }
        val after = dataSource.count()
        model.addAttribute("purgedCount", before - after)
        log.info("Purged {} on view '{}'", before - after, view)

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
               auth: Authentication
    ): String {
        val user = auth.getUser()
        if (createPermission.validate(user).not()) {
            model.addAttribute("permission", createPermission.permissionString)
            model.addAttribute("user", user)
            return "admin403"
        }

        if (!createEnabled)
            return "redirect:/admin/control/$view/"

        val entity = supplier.get()
        updateEntity(descriptor, user, entity, dto, file0, file1)
        entity.id = 0
        if (onEntityPreSave(entity, auth))
            dataSource.save(entity)
        onEntityChanged(entity)
        return "redirect:/admin/control/$view/"
    }

    @PostMapping("/edit/{id}")
    fun edit(@PathVariable id: Int,
             @ModelAttribute(binding = false) dto: T,
             @RequestParam(required = false) file0: MultipartFile?,
             @RequestParam(required = false) file1: MultipartFile?,
             model: Model,
             auth: Authentication
    ): String {
        val user = auth.getUser()
        if (editPermission.validate(user).not()) {
            model.addAttribute("permission", editPermission.permissionString)
            model.addAttribute("user", user)
            return "admin403"
        }

        val entity = dataSource.findById(id)
        if (entity.isEmpty) {
            return "redirect:/admin/control/$view/edit/$id"
        }
        val actualEntity = entity.orElseThrow()
        if (!editPermissionCheck(user, actualEntity)) {
            model.addAttribute("user", user)
            return "admin403"
        }

        if (!editEnabled)
            return "redirect:/admin/control/$view/"

        updateEntity(descriptor, user, actualEntity, dto, file0, file1)
        actualEntity.id = id
        if (onEntityPreSave(actualEntity, auth))
            dataSource.save(actualEntity)
        onEntityChanged(actualEntity)
        return "redirect:/admin/control/$view"
    }

    private fun updateEntity(descriptor: OverviewBuilder<T>, user: CmschUser, entity: T, dto: T, file0: MultipartFile?, file1: MultipartFile?) {
        descriptor.getInputs().forEach {
            if (it.first is KMutableProperty1<out Any, *> && !it.second.ignore && it.second.minimumRole.value <= user.role.value) {
                when {
                    it.second.interpreter == INTERPRETER_INHERIT && it.second.type == INPUT_TYPE_FILE -> {
                        when (it.second.fileId) {
                            "0" -> {
                                file0?.uploadFile(view)?.let { file ->
                                    (it.first as KMutableProperty1<out Any, *>).setter.call(entity, "$view/$file")
                                }
                            }
                            "1" -> {
                                file1?.uploadFile(view)?.let { file ->
                                    (it.first as KMutableProperty1<out Any, *>).setter.call(entity, "$view/$file")
                                }
                            }
                            else -> {
                                log.error("Invalid file field name: file${it.second.fileId}")
                            }
                        }
                    }
                    (it.second.interpreter == INTERPRETER_INHERIT || it.second.interpreter == INTERPRETER_SEARCH) && it.second.type != INPUT_TYPE_FILE -> {
                        (it.first as KMutableProperty1<out Any, *>).setter.call(entity, it.first.getter.call(dto))
                    }
                    it.second.interpreter == "path" -> {
                        (it.first as KMutableProperty1<out Any, *>).setter.call(entity, it.first.getter.call(dto)
                            .toString()
                            .lowercase()
                            .replace(" ", "-")
                            .replace(Regex("[^a-z0-9-.]"), ""))
                    }

                }
            }
        }
    }

    @GetMapping("/resource")
    fun resource(model: Model, auth: Authentication): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (createPermission.validate(user).not() || showPermission.validate(user).not()) {
            model.addAttribute("permission", createPermission.permissionString)
            model.addAttribute("user", user)
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
        return descriptor.exportToCsv(filterOverview(user, fetchOverview()).toList()).toByteArray()
    }

    @PostMapping("/import/csv")
    fun import(file: MultipartFile?, model: Model, auth: Authentication): String {
        val user = auth.getUser()
        if (!importEnabled || !createEnabled || !createPermission.validate(user)) {
            throw IllegalStateException("Insufficient permissions")
        }

        val out = ByteArrayOutputStream()
        file?.inputStream?.transferTo(out)
        val rawEntities = out.toString().split("\n").stream()
            .map { entity -> entity.split(";").map { it.trim() } }
            .skip(1)
            .toList()
        log.info("Importing {} bytes ({} lines) into {}", out.size(), rawEntities.size, view)
        val before = dataSource.count()
        importService.importEntities(dataSource, rawEntities, supplier, descriptor.getImportModifiers())
        val after = dataSource.count()
        model.addAttribute("importedCount", after - before)

        model.addAttribute("title", titlePlural)
        model.addAttribute("view", view)
        model.addAttribute("user", user)
        adminMenuService.addPartsForMenu(user, model)
        return "resource"
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

    open fun filterOverview(user: CmschUser, rows: Iterable<T>): Iterable<T> {
        return rows
    }

    open fun editPermissionCheck(user: CmschUser, entity: T): Boolean {
        return true
    }

    open fun fetchOverview(): Iterable<T> {
        return dataSource.findAll()
    }

}
