package hu.bme.sch.cmsch.component.login

import hu.bme.sch.cmsch.component.app.ApplicationComponent
import hu.bme.sch.cmsch.config.StartupPropertyConfig
import hu.bme.sch.cmsch.service.JwtTokenProvider
import hu.bme.sch.cmsch.util.getUserOrNull
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URI

@Controller
@ConditionalOnBean(LoginComponent::class)
class AuthschLoginController(
    private val applicationComponent: ApplicationComponent,
    private val jwtTokenProvider: JwtTokenProvider,
    private val startupPropertyConfig: StartupPropertyConfig,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @ResponseBody
    @GetMapping("/control/logged-out")
    fun loggedOut(): String {
        return "Sikeres kijelentkezés!"
    }

    @GetMapping("/control/post-login")
    fun postLogin(httpResponse: HttpServletResponse, auth: Authentication?) {
        httpResponse.sendRedirect(
            if (auth != null && auth.isAuthenticated)
                "/control/open-site"
            else
                "/control/logged-out?error=not-logged-in"
        )
    }

    @GetMapping("/control/login")
    fun loginDefault(): String {
        return "redirect:${applicationComponent.siteUrl.getValue()}login"
    }

    @GetMapping("/control/logout")
    fun logout(auth: Authentication?, response: HttpServletResponse): String {
        log.info("Logging out from user {}", auth?.getUserOrNull()?.internalId ?: "n/a")

        try {
            createJwtCookies(null).forEach { response.addCookie(it) }
            SecurityContextHolder.getContext().authentication = null
        } catch (e: Exception) {
            // It should be logged out anyway
        }
        return "redirect:${applicationComponent.siteUrl.getValue()}?logged-out=true"
    }

    @GetMapping("/control/open-site")
    fun openSite(auth: Authentication?, response: HttpServletResponse): String {
        if (auth != null) {
            if (auth.principal !is CmschUser) {
                log.error("User is not CmschUser {} {}", auth, auth.javaClass.simpleName)
                return "redirect:${applicationComponent.siteUrl.getValue()}?error=cannot-generate-jwt"
            }
            val jwtToken = jwtTokenProvider.createToken(auth.principal as CmschUser)
            createJwtCookies(jwtToken).forEach { response.addCookie(it) }

            return "redirect:${applicationComponent.siteUrl.getValue()}"
        }
        return "redirect:${applicationComponent.siteUrl.getValue()}"
    }

    @ResponseBody
    @PostMapping("/api/control/refresh")
    fun refreshToken(auth: Authentication?, response: HttpServletResponse): ResponseEntity<String> {
        if (auth == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        createJwtCookies(jwtTokenProvider.refreshToken(auth)).forEach { response.addCookie(it) }

        return ResponseEntity.ok().build()
    }

    private fun getDomainFromUrl(url: String): String {
        return URI(url).host
    }

    private fun createJwtCookies(value: String?): List<Cookie> {
        return listOf(
            Cookie("jwt", value).apply {
                isHttpOnly = true
                path = "/"
                maxAge = startupPropertyConfig.sessionValidityInMilliseconds.toInt() / 1000
                secure = true
                domain = getDomainFromUrl(applicationComponent.siteUrl.getValue())
            },
            Cookie("jwt", value).apply {
                isHttpOnly = true
                path = "/"
                maxAge = startupPropertyConfig.sessionValidityInMilliseconds.toInt() / 1000
                secure = true
                domain = getDomainFromUrl(applicationComponent.adminSiteUrl.getValue())
            },
        )
    }
}
