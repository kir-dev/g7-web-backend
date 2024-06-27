package hu.bme.sch.cmsch.service

import hu.bme.sch.cmsch.config.StartupPropertyConfig
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.http.CacheControl
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.*
import kotlin.io.path.exists

@Service
class FilesystemStorageService(private val startupPropertyConfig: StartupPropertyConfig) : StorageService {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val handler = registry.addResourceHandler("/cdn/**")
            .addResourceLocations("file:${getFileStoragePath()}")
        if (startupPropertyConfig.cdnCacheMaxAge > 0) {
            handler.setCacheControl(CacheControl.maxAge(Duration.ofSeconds(startupPropertyConfig.cdnCacheMaxAge)))
        }
    }

    override fun getObjectUrl(fullName: String): Optional<String> {
        val url = if (fullName.startsWith("/cdn/"))
            fullName
        else
            "/cdn${if (fullName.startsWith("/")) "" else "/"}$fullName"

        if (Paths.get(getFileStoragePath(), url.replace("/cdn/", "/")).exists()) {
            return Optional.of(url)
        }
        return Optional.empty()
    }


    override fun saveNamedObject(path: String, name: String, file: MultipartFile): Optional<String> {
        if (file.isEmpty || file.contentType == null)
            return Optional.empty()

        return saveNamedObject(path, name, file.bytes)
    }

    override fun saveNamedObject(path: String, name: String, data: ByteArray): Optional<String> {
        val storagePath = getFileStoragePath()
        val dir = File(storagePath, path)
        dir.mkdirs()

        try {
            val filePath = Paths.get(storagePath, path, name)
            Files.write(filePath, data)
        } catch (e: IOException) {
            log.error("Failed to write object to filesystem", e)
            return Optional.empty()
        }
        return Optional.of(name)
    }

    override fun readObject(fullName: String): Optional<ByteArray> {
        try {
            return Optional.of(ClassPathResource(fullName).inputStream.readAllBytes())
        } catch (e: Throwable) {
            log.error("Failed to read object from classpath", e)
        }
        try {
            return Optional.of(Files.readAllBytes(Paths.get(getFileStoragePath(), fullName)))
        } catch (e: Throwable) {
            log.error("Failed to read object from filesystem", e)
        }
        return Optional.empty()
    }

    private fun getFileStoragePath(): String = if (!startupPropertyConfig.external.startsWith("/")) {
        System.getProperty("user.dir") + "/" + startupPropertyConfig.external
    } else {
        startupPropertyConfig.external
    }
}
