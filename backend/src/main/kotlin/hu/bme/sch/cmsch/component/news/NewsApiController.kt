package hu.bme.sch.cmsch.component.news

import com.fasterxml.jackson.annotation.JsonView
import hu.bme.sch.cmsch.dto.Preview
import hu.bme.sch.cmsch.model.RoleType
import hu.bme.sch.cmsch.util.getUserOrNull
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["\${cmsch.frontend.production-url}"], allowedHeaders = ["*"])
@ConditionalOnBean(NewsComponent::class)
class NewsApiController(
    private val newsRepository: NewsRepository,
    private val newsComponent: NewsComponent
) {

    @JsonView(Preview::class)
    @GetMapping("/news")
    @Operation(summary = "Detailed view of the available news")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "The list of all the news available"),
        ApiResponse(responseCode = "403", description = "This endpoint is not available for the given auth header")
    ])
    fun news(auth: Authentication): ResponseEntity<NewsView> {
        val user = auth.getUserOrNull()
        if (!newsComponent.minRole.isAvailableForRole(user?.role ?: RoleType.GUEST))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        return ResponseEntity.ok(NewsView(
            news = newsRepository.findAllByVisibleTrueOrderByTimestampDesc()
                .filter { (user?.role ?: RoleType.GUEST).value >= it.minRole.value }
        ))
    }

}
