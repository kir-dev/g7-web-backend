package hu.bme.sch.cmsch.component.token

import com.fasterxml.jackson.annotation.JsonView
import hu.bme.sch.cmsch.component.app.ApplicationComponent
import hu.bme.sch.cmsch.config.OwnershipType
import hu.bme.sch.cmsch.config.StartupPropertyConfig
import hu.bme.sch.cmsch.dto.FullDetails
import hu.bme.sch.cmsch.util.getUser
import hu.bme.sch.cmsch.util.getUserOrNull
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/api")
@ConditionalOnBean(TokenComponent::class)
class TokenApiController(
    private val tokens: TokenCollectorService,
    private val startupPropertyConfig: StartupPropertyConfig,
    private val applicationComponent: ApplicationComponent
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @JsonView(FullDetails::class)
    @GetMapping("/tokens")
    fun riddle(auth: Authentication?): ResponseEntity<TokenView> {
        val user = auth.getUserOrNull() ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(tokens.getTokenViewForUser(user))
    }

    @ResponseBody
    @JsonView(FullDetails::class)
    @PostMapping("/token/{token}")
    fun submitToken(@PathVariable token: String, auth: Authentication): TokenSubmittedView {
        return try {
            when (startupPropertyConfig.tokenOwnershipMode) {
                OwnershipType.USER -> {
                    tokens.collectToken(auth.getUser(), token)
                }
                OwnershipType.GROUP -> {
                    tokens.collectTokenForGroup(auth.getUser(), token)
                }
            }
        } catch (e: Throwable) {
            log.error("Failed to redeem token: '{}'", token, e)
            TokenSubmittedView(TokenCollectorStatus.CANNOT_COLLECT, null, null, null)
        }
    }

    @GetMapping("/qr/{token}")
    fun readQrManually(@PathVariable token: String, auth: Authentication?): String {
        return try {
            val user = auth?.getUserOrNull()
            if (user == null) {
                "redirect:${applicationComponent.siteUrl.getValue()}login"
            } else {
                collectToken(auth, token)
            }
        } catch (e: Throwable) {
            log.error("Failed to redeem token: '{}'", token, e)
            "redirect:${applicationComponent.siteUrl.getValue()}login?error=failed-to-redeem"
        }
    }

    private fun collectToken(auth: Authentication, token: String): String {
        return when (startupPropertyConfig.tokenOwnershipMode) {
            OwnershipType.USER -> {
                val response = tokens.collectToken(auth.getUser(), token)
                log.info("Token collected for USER '{}' token '{}'", auth.getUser().userName, token)
                "redirect:${applicationComponent.siteUrl.getValue()}token-scanned?status=${response.status.name}" +
                        "&title=${URLEncoder.encode(response.title ?: "", StandardCharsets.UTF_8.toString())}" +
                        "&description=${URLEncoder.encode(response.description ?: "", StandardCharsets.UTF_8.toString())}" +
                        "&icon=${URLEncoder.encode(response.iconUrl ?: "", StandardCharsets.UTF_8.toString())}"
            }
            OwnershipType.GROUP -> {
                val response = tokens.collectTokenForGroup(auth.getUser(), token)
                log.info("Token collected for GROUP by user '{}' token '{}'", auth.getUser().userName, token)
                "redirect:${applicationComponent.siteUrl.getValue()}token-scanned?status=${response.status.name}" +
                        "&title=${URLEncoder.encode(response.title ?: "", StandardCharsets.UTF_8.toString())}" +
                        "&description=${URLEncoder.encode(response.description ?: "", StandardCharsets.UTF_8.toString())}" +
                        "&icon=${URLEncoder.encode(response.iconUrl ?: "", StandardCharsets.UTF_8.toString())}"
            }
        }
    }

}
