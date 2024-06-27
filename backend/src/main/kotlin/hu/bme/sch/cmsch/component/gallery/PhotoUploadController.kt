package hu.bme.sch.cmsch.component.gallery

import hu.bme.sch.cmsch.component.app.ApplicationComponent
import hu.bme.sch.cmsch.service.*
import hu.bme.sch.cmsch.util.getUser
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.math.absoluteValue

@Controller
@RequestMapping("/admin/control/upload-photo")
@ConditionalOnBean(GalleryComponent::class)
class PhotoUploadController(
    private val adminMenuService: AdminMenuService,
    private val applicationComponent: ApplicationComponent,
    private val auditLog: AuditLogService,
    private val galleryService: GalleryService,
    private val storageService: StorageService
) {

    private val permissionControl = ControlPermissions.PERMISSION_CONTROL_GALLERY

    @PostConstruct
    fun init() {
        adminMenuService.registerEntry(
            GalleryComponent::class.java.simpleName,
            AdminMenuEntry(
                "Képfeltöltés",
                "cloud_upload",
                "/admin/control/upload-photo",
                2,
                permissionControl
            )
        )
    }

    @GetMapping("")
    fun view(model: Model, auth: Authentication, @RequestParam(defaultValue = "no") uploaded: String): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (permissionControl.validate(user).not()) {
            model.addAttribute("permission", permissionControl.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, "file-upload", "GET /upload-photo", permissionControl.permissionString)
            return "admin403"
        }

        model.addAttribute("user", user)
        model.addAttribute("uploaded", uploaded)
        model.addAttribute("baseUrl", applicationComponent.adminSiteUrl.getValue())
        model.addAttribute("permission", permissionControl.permissionString)

        return "uploadPhoto"
    }

    @PostMapping
    fun uploadImage(model: Model, auth: Authentication, @RequestParam names: List<String>, @RequestBody files: List<MultipartFile>): String {
        val user = auth.getUser()
        adminMenuService.addPartsForMenu(user, model)
        if (permissionControl.validate(user).not()) {
            model.addAttribute("permission", permissionControl.permissionString)
            model.addAttribute("user", user)
            auditLog.admin403(user, "file-upload", "POST /upload-photo", permissionControl.permissionString)
            return "admin403"
        }

        if (names.size != files.size) {
            throw IllegalArgumentException("The length of names and files does not match")
        }
        val newNames = mutableListOf<String>()
        for ((file, name) in files.zip(names)) {
            val originalFilename = file.originalFilename ?: ""
            val newName = name.replace(" ", "_").replace(Regex("[^A-Za-z0-9_]+"), "").uppercase() +
                    "_${Random().nextLong().absoluteValue.toString(36).uppercase()}" +
                    originalFilename.substring(if (originalFilename.contains(".")) originalFilename.lastIndexOf('.') else 0)
            val fileUrl = "${applicationComponent.adminSiteUrl.getValue()}cdn/public/${newName}"
            galleryService.savePhoto(GalleryEntity(title = name, highlighted = false, url = fileUrl))
            storageService.saveNamedObject("public", newName, file)
            newNames.add(newName)
        }
        return "redirect:/admin/control/upload-photo?uploaded=${newNames.joinToString(",")}"
    }

}
