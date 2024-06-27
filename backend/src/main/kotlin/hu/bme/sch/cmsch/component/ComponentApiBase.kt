package hu.bme.sch.cmsch.component

import hu.bme.sch.cmsch.component.app.MenuService
import hu.bme.sch.cmsch.model.RoleType
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.getUser
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartRequest
import jakarta.annotation.PostConstruct

abstract class ComponentApiBase(
    val adminMenuService: AdminMenuService,
    private val componentClass: Class<*>,
    val component: ComponentBase,
    private val permissionToShow: PermissionValidator,
    private val componentCategoryName: String = "",
    private val componentMenuName: String,
    private val componentMenuIcon: String = "settings",
    private val componentMenuPriority: Int = 100,
    private val insertComponentCategory: Boolean = true,
    private val componentCategory: String = componentClass.simpleName,
    private val menuService: MenuService,
    private val auditLogService: AuditLogService,
    private val storageService: StorageService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        if (insertComponentCategory) {
            adminMenuService.registerCategory(
                componentCategory,
                AdminMenuCategory(componentCategoryName, component.menuPriority)
            )
        }
        adminMenuService.registerEntry(
            componentCategory, AdminMenuEntry(
                componentMenuName, componentMenuIcon,
                "/admin/control/component/${component.component}/settings",
                componentMenuPriority,
                permissionToShow
            )
        )
        onInit()
    }

    @GetMapping("/settings")
    fun settings(auth: Authentication, model: Model): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (!permissionToShow.validate(user)) {
            model.addAttribute("permission", permissionToShow.permissionString)
            model.addAttribute("user", user)
            auditLogService.admin403(user, component.component,
                "GET ${component.component}/settings", permissionToShow.permissionString)
            return "admin403"
        }

        model.addAttribute("component", component.component)
        model.addAttribute("title", componentMenuName)
        model.addAttribute("settings", component.allSettings)
        model.addAttribute("componentNames", menuService.getComponentNames())
        model.addAttribute("user", user)
        model.addAttribute("permission", permissionToShow.permissionString)

        return "componentSettings"
    }

    @PostMapping("/settings")
    fun update(
        auth: Authentication,
        model: Model,
        @RequestParam allRequestParams: Map<String, String>,
        multipartRequest: MultipartRequest
    ): String {
        val user = auth.getUser()
        if (!permissionToShow.validate(user)) {
            model.addAttribute("permission", permissionToShow.permissionString)
            model.addAttribute("user", user)
            auditLogService.admin403(user, component.component,
                "POST ${component.component}/settings", permissionToShow.permissionString)
            return "admin403"
        }
        val newValues = StringBuilder("component-edit new value: ")
        component.allSettings.forEach { setting ->
            when (setting.type) {
                SettingType.BOOLEAN -> {
                    val parsedValue = allRequestParams[setting.property] != null && allRequestParams[setting.property] != "off"
                    log.info("Changing the value of {}.{} to '{}'", setting.component, setting.property, parsedValue)
                    newValues.append(setting.property).append("=").append(parsedValue).append(", ")
                    setting.setValue(if (parsedValue) "true" else "false")
                }
                SettingType.IMAGE -> {
                    multipartRequest.fileMap[setting.property]?.let {
                        if (it.size > 0) {
                            log.info("Uploading image {}.{} size: {}", setting.component, setting.property, it.size)
                            newValues.append(setting.property).append("=size@").append(it.size).append(", ")
                            storageService.saveNamedObject("manifest", setting.rawValue.split("/").last(), it)
                        }
                    }
                }
                else -> {
                    allRequestParams[setting.property]?.let {
                        log.info("Changing the value of {}.{} to '{}'", setting.component, setting.property, it)
                        newValues.append(setting.property).append("=").append(it).append(", ")
                        setting.setValue(it)
                    }
                }
            }
        }
        component.persistChanges()
        component.onPersist()
        component.onFirePersistEvent()
        auditLogService.edit(user, component.component, newValues.toString())
        RoleType.entries.forEach { role -> menuService.regenerateMenuCache(role) }
        onUpdate()
        return "redirect:/admin/control/component/${component.component}/settings"
    }

    open fun onUpdate() {
        // Settings updated
    }

    open fun onInit() {
        // The component is loaded
    }

}
