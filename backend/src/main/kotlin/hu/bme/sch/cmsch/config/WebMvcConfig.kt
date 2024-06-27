package hu.bme.sch.cmsch.config

import hu.bme.sch.cmsch.service.StorageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val storageService: StorageService,
    @Value("\${cmsch.frontend.production-url:*}") private val productionUrl: String,
    @Value("\${cmsch.backend.allowed-origin-patterns:*}") private val allowedOrigins: List<String>
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        storageService.addResourceHandlers(registry)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        arrayOf("/api/**", "/manifest/**", "/cdn/**").forEach {
            registry.addMapping(it)
                .allowedOrigins(productionUrl)
                .allowCredentials(true)
                .allowedOriginPatterns(*allowedOrigins.toTypedArray())
                .allowedMethods(*HttpMethod.values().map(HttpMethod::name).toTypedArray())
        }
    }
}
