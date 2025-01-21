package hu.bme.sch.cmsch.controller.dashboard

import hu.bme.sch.cmsch.CMSCH_VERSION
import hu.bme.sch.cmsch.admin.dashboard.DashboardComponent
import hu.bme.sch.cmsch.admin.dashboard.DashboardPage
import hu.bme.sch.cmsch.admin.dashboard.DashboardPermissionCard
import hu.bme.sch.cmsch.admin.dashboard.DashboardTableCard
import hu.bme.sch.cmsch.component.app.ApplicationComponent
import hu.bme.sch.cmsch.component.login.CmschUser
import hu.bme.sch.cmsch.config.ComponentLoadConfig
import hu.bme.sch.cmsch.config.StartupPropertyConfig
import hu.bme.sch.cmsch.service.AdminMenuService
import hu.bme.sch.cmsch.service.AuditLogService
import hu.bme.sch.cmsch.service.ControlPermissions
import hu.bme.sch.cmsch.service.TimeService
import hu.bme.sch.cmsch.statistics.UserActivityFilter
import org.apache.catalina.util.ServerInfo
import org.springframework.boot.info.BuildProperties
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.text.SimpleDateFormat
import java.util.*

@Controller
@RequestMapping("/admin/control/instance-info")
class InstanceInfoDashboard(
    adminMenuService: AdminMenuService,
    applicationComponent: ApplicationComponent,
    auditLogService: AuditLogService,
    env: Environment,
    startupPropertyConfig: StartupPropertyConfig,
    componentLoadConfig: ComponentLoadConfig,
    buildProperties: BuildProperties?,
    private val userActivityFilter: Optional<UserActivityFilter>,
    private val clock: TimeService,
) : DashboardPage(
    "instance-info",
    "Szerver adatok",
    "Az alkalmazás adatai",
    false,
    adminMenuService,
    applicationComponent,
    auditLogService,
    ControlPermissions.PERMISSION_SHOW_INSTANCE,
    ApplicationComponent.DEVELOPER_CATEGORY,
    "info",
    6
) {

    private val systemInfo = DashboardTableCard(
        0,
        "Rendszer Adatok",
        "",
        listOf("Property", "Value"),
        listOf(
            listOf("OS name",               System.getProperty("os.name")),
            listOf("OS version",            System.getProperty("os.version")),
            listOf("Architecture",          System.getProperty("os.arch")),
            listOf("JVM Version",           System.getProperty("java.runtime.version")),
            listOf("JVM Vendor",            System.getProperty("java.vm.vendor")),
            listOf("Available processors",  Runtime.getRuntime().availableProcessors().toString()),
            listOf("Max memory",            "${Runtime.getRuntime().maxMemory() / (1000 * 1000)} MB"),
            listOf("Server version",        ServerInfo.getServerInfo()),
            listOf("Server built",          ServerInfo.getServerBuilt()),
            listOf("Server number",         ServerInfo.getServerNumber()),
            listOf("Build Artifact",        buildProperties?.artifact ?: "n/a"),
            listOf("Build Group",           buildProperties?.group ?: "n/a"),
            listOf("Build Name",            buildProperties?.name ?: "n/a"),
            listOf("Build Time",            buildProperties?.time?.toString() ?: "n/a"),
            listOf("Build version",         buildProperties?.version ?: "n/a"),
            listOf("Profiles",              env.activeProfiles.joinToString(", ")),
            listOf("CMSCH version",         CMSCH_VERSION),
        ),
        false
    )

    private val deploymentInfo = DashboardTableCard(
        1,
        "Példány Adatok",
        "",
        listOf("Property", "Value"),
        listOf(
            listOf("Sysadmins",                     startupPropertyConfig.sysadmins),
            listOf("External resources",            startupPropertyConfig.external),
            listOf("Audit log location",            startupPropertyConfig.auditLog),
            listOf("Time zone id",                  startupPropertyConfig.zoneId),
            listOf("Mailgun token length",          startupPropertyConfig.mailgunToken.length.toString()),
            listOf("Session validity (ms)",         startupPropertyConfig.sessionValidityInMilliseconds.toString()),
            listOf("Profile QR enabled",            startupPropertyConfig.profileQrEnabled.toString()),
            listOf("Profile QR prefix",             startupPropertyConfig.profileQrPrefix),
            listOf("Profile generation target",     startupPropertyConfig.profileGenerationTarget),
            listOf("Profile QR code size (px)",     startupPropertyConfig.profileQrCodeSize.toString()),
            listOf("Task ownership mode",           startupPropertyConfig.taskOwnershipMode.name),
            listOf("Riddle ownership mode",         startupPropertyConfig.riddleOwnershipMode.name),
            listOf("Token ownership mode",          startupPropertyConfig.tokenOwnershipMode.name),
            listOf("Challenge ownership mode",      startupPropertyConfig.challengeOwnershipMode.name),
            listOf("Race ownership mode",           startupPropertyConfig.raceOwnershipMode.name),
            listOf("Max threads",                   env.getProperty("server.tomcat.threads.max", "n/a")),
            listOf("Min spare threads",             env.getProperty("server.tomcat.threads.min-spare", "n/a")),
            listOf("Max file size",                 env.getProperty("spring.servlet.multipart.max-file-size", "n/a")),
            listOf("Max request size",              env.getProperty("spring.servlet.multipart.max-request-size", "n/a")),
            listOf("Datasource url",                env.getProperty("spring.datasource.url", "n/a")),
            listOf("Api docs enabled",              env.getProperty("springdoc.api-docs.enabled", "n/a")),
            listOf("Swagger enabled",               env.getProperty("springdoc.swagger-ui.enabled", "n/a")),
            listOf("Include stacktrace",            env.getProperty("server.error.include-stacktrace", "n/a")),
        ),
        false
    )

    private val componentInfo = DashboardTableCard(
        2,
        "Komponens Adatok",
        "",
        listOf("Property", "Value"),
        listOf(
            listOf("admission",         componentLoadConfig.admission.toString()),
            listOf("app",               componentLoadConfig.app.toString()),
            listOf("bmejegy",           componentLoadConfig.bmejegy.toString()),
            listOf("challenge",         componentLoadConfig.challenge.toString()),
            listOf("countdown",         componentLoadConfig.countdown.toString()),
            listOf("debt",              componentLoadConfig.debt.toString()),
            listOf("event",             componentLoadConfig.event.toString()),
            listOf("staticPage",        componentLoadConfig.staticPage.toString()),
            listOf("groupselection",    componentLoadConfig.groupselection.toString()),
            listOf("home",              componentLoadConfig.home.toString()),
            listOf("impressum",         componentLoadConfig.impressum.toString()),
            listOf("leaderboard",       componentLoadConfig.leaderboard.toString()),
            listOf("location",          componentLoadConfig.location.toString()),
            listOf("login",             componentLoadConfig.login.toString()),
            listOf("news",              componentLoadConfig.news.toString()),
            listOf("profile",           componentLoadConfig.profile.toString()),
            listOf("qrFight",           componentLoadConfig.qrFight.toString()),
            listOf("race",              componentLoadConfig.race.toString()),
            listOf("riddle",            componentLoadConfig.riddle.toString()),
            listOf("form",              componentLoadConfig.form.toString()),
            listOf("task",              componentLoadConfig.task.toString()),
            listOf("team",              componentLoadConfig.team.toString()),
            listOf("token",             componentLoadConfig.token.toString()),
            listOf("communities",       componentLoadConfig.communities.toString()),
        ),
        false
    )

    private val permissionCard = DashboardPermissionCard(
        3,
        permission = showPermission.permissionString,
        description = "Ez a jog szükséges ennek az oldalnak az olvasásához.",
        wide = false
    )

    override fun getComponents(user: CmschUser, requestParams: Map<String, String>): List<DashboardComponent> {
        return listOf(
            permissionCard,
            getStatistics(),
            systemInfo,
            deploymentInfo,
            componentInfo,
        )
    }

    private val formatter = SimpleDateFormat("yyyy.MM.dd. HH:mm:ss")

    private fun getStatistics() = DashboardTableCard(
        4,
        "Élő Adatok",
        "",
        listOf("Property", "Value"),
        listOf(
            listOf("Time",          formatter.format(clock.getTimeInSeconds() * 1000)),
            listOf("Used memory",   "${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) 
                                        / (1000 * 1000)} MB"),
            listOf("RPM",           userActivityFilter.map { it.rpm.toString() }.orElse("")),
            listOf("Users in 5m",   userActivityFilter.map { it.usersIn5Minutes.toString() }.orElse("")),
            listOf("Users in 30m",  userActivityFilter.map { it.usersIn30Minutes.toString() }.orElse("")),
        ),
        false,
        exportable = true
    )
}
