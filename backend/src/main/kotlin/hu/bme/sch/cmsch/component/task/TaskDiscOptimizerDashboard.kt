package hu.bme.sch.cmsch.component.task

import com.google.common.util.concurrent.ThreadFactoryBuilder
import hu.bme.sch.cmsch.admin.dashboard.*
import hu.bme.sch.cmsch.component.app.ApplicationComponent
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.config.StartupPropertyConfig
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.FilesystemStorageService
import hu.bme.sch.cmsch.service.ImplicitPermissions
import hu.bme.sch.cmsch.util.getUser
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.LongAdder
import javax.imageio.ImageIO

private const val VIEW = "task-disc-optimize"

@Controller
@RequestMapping("/admin/control/$VIEW")
@ConditionalOnBean(value = [TaskComponent::class, FilesystemStorageService::class])
class TaskDiscOptimizerDashboard(
    adminMenuService: AdminMenuService,
    applicationComponent: TaskComponent,
    auditLogService: AuditLogService,
    private val startupPropertyConfig: StartupPropertyConfig,
    private val imageRescaleService: TaskImageRescaleService
) : DashboardPage(
    view = VIEW,
    title = "Disc Optimizer",
    description = "",
    wide = false,
    adminMenuService = adminMenuService,
    component = applicationComponent,
    auditLog = auditLogService,
    showPermission = ImplicitPermissions.PERMISSION_SUPERUSER_ONLY,
    adminMenuCategory = ApplicationComponent.DEVELOPER_CATEGORY,
    adminMenuIcon = "delete",
    adminMenuPriority = 21
) {

    companion object {
        @JvmStatic
        val lock = Any()
    }

    private val log = LoggerFactory.getLogger(javaClass)

    internal val processStatus = AtomicReference("nincs exportálva")
    internal val processCount = LongAdder()
    internal val processReduction = LongAdder()
    internal val processRunning = AtomicBoolean(false)

    private val threadFactory = ThreadFactoryBuilder()
        .setUncaughtExceptionHandler { _, e ->
            log.error("Uncaught exception during Souvenir export", e)
            processStatus.set("Error: ${e.message}")
        }
        .setNameFormat("optimizer-pool-%d")
        .build()
    private val executorService = Executors.newThreadPerTaskExecutor(threadFactory)

    override fun getComponents(user: CmschUser, requestParams: Map<String, String>): List<DashboardComponent> {
        return listOf(
            exportStatus(),
            exportPanel(),
            permissionCard
        )
    }

    private fun exportStatus(): DashboardTableCard {
        return DashboardTableCard(
            id = 2,
            title = "Optimalizálás eredménye",
            description = "Ha üres, akkor még nem volt optimalizálva",
            header = listOf("Kulcs", "Érték"),
            content = listOf(
                listOf("Exportálás folyamatban", if (processRunning.get()) "igen" else "nem"),
                listOf("Exportálás státusza", processStatus.get()),
                listOf("Counter", processCount.sum().toString()),
                listOf("Spórolás (byte)", processReduction.sum().toString()),
            ),
            wide = wide,
            exportable = false
        )
    }

    fun exportPanel(): DashboardFormCard {
        return DashboardFormCard(
            id = 3,
            wide = wide,
            title = "Optimalizálás indítása",
            description = "",
            content = listOf(
            ),
            buttonCaption = "Indítás",
            buttonIcon = "play_arrow",
            action = "optimize",
            method = "post",
            multipartForm = false,
        )
    }

    @PostMapping("/optimize")
    fun optimize(auth: Authentication, @RequestParam allRequestParams: Map<String, String>): String {
        val user = auth.getUser()
        if (!showPermission.validate(user)) {
            throw IllegalStateException("Insufficient permissions")
        }

        synchronized(lock) {
            if (processRunning.get())
                return dashboardPage(view = VIEW, card = 2, message = "Már fut exportálás")
            processRunning.set(true)
            executorService.submit(TaskImageOptimizerTask(
                processStatus = processStatus,
                processRunning = processRunning,
                processCount = processCount,
                processReduction = processReduction,
                startupPropertyConfig = startupPropertyConfig,
                imageRescaleService = imageRescaleService,
            ))
        }

        return dashboardPage(view = VIEW, card = 2, message = "Exportálás beütemezve")
    }


    private val permissionCard = DashboardPermissionCard(
        id = 1,
        permission = showPermission.permissionString,
        description = "Ez a jog szükséges ennek az oldalnak az olvasásához.",
        wide = false
    )

}

class TaskImageOptimizerTask(
    private val processStatus: AtomicReference<String>,
    private val processRunning: AtomicBoolean,
    private val processCount: LongAdder,
    private val processReduction: LongAdder,
    private val startupPropertyConfig: StartupPropertyConfig,

    private val imageRescaleService: TaskImageRescaleService,
) : Runnable {

    override fun run() {
        processStatus.set("Optimizer elindult")

        Files.walk(Path.of(startupPropertyConfig.external, "task"))
            .filter {
                val fileName = it.fileName.toString().lowercase()
                val image = fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
                println("$fileName $image")
                return@filter image
            }
            .forEach { !resizeImage(it.toFile()) }

        processStatus.set("Optimizer kész")
        processRunning.set(false)
    }

    private fun resizeImage(inputFile: File): Boolean {
        processCount.increment()
        val oldLength = inputFile.length()
        println("RESCALE $inputFile $oldLength")
        val MAX_HEIGHT_WIDTH = 1000

        val kiloByte = 1024
        if (inputFile.length() > (120 * kiloByte)) {
            val inputImage = ImageIO.read(inputFile)
            if (inputImage.height > (MAX_HEIGHT_WIDTH + 1) || inputImage.width > (MAX_HEIGHT_WIDTH + 1)) {
                val resizedImage = imageRescaleService.resizeImage(inputImage, MAX_HEIGHT_WIDTH, MAX_HEIGHT_WIDTH)
                val format = inputFile.name.substringAfterLast(".").lowercase()
                println(" - FORMAT: $format")

                ImageIO.write(resizedImage, format, inputFile)
                val newLength = inputFile.length()
                val diff = oldLength - newLength
                println(" - NEW LENGTH: $newLength DIFF: $diff")
                processReduction.add(diff)
                return true
            }
            println(" - SMALL ENOUGH by scale")
            return false
        } else {
            println(" - SMALL ENOUGH by volume")
            return false
        }
    }

}
