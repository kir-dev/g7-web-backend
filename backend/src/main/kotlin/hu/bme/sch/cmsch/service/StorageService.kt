package hu.bme.sch.cmsch.service

import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import java.util.*

interface StorageService {

    // Only needed when using FilesystemStorageService to serve files automatically
    fun addResourceHandlers(registry: ResourceHandlerRegistry) {}

    fun exists(fullName: String): Boolean = getObjectUrl(fullName).isPresent

    fun exists(path: String, name: String): Boolean = getObjectUrl(path, name).isPresent

    fun getObjectUrl(fullName: String): Optional<String>

    fun getObjectUrl(path: String, name: String): Optional<String> = getObjectUrl("${path}/${name}")

    /**
     * Commit a multipart file into storage
     * @return hashed file name if successful
     */
    fun saveObject(path: String, file: MultipartFile): Optional<String> =
        saveNamedObject(path, hashName(file.originalFilename ?: ""), file)

    /**
     * Commit an object into storage
     * @return hashed file name if successful
     */
    fun saveObject(path: String, fileName: String, data: ByteArray): Optional<String> =
        saveNamedObject(path, hashName(fileName), data)

    /**
     * Commit a multipart file into storage with a predefined name
     * @return file name if successful
     */
    fun saveNamedObject(path: String, name: String, file: MultipartFile): Optional<String>

    /**
     * Commit an object into storage with a predefined name
     * @return file name if successful
     */
    fun saveNamedObject(path: String, name: String, data: ByteArray): Optional<String>

    fun readObject(path: String, name: String): Optional<ByteArray> = readObject("${path}/${name}")

    fun readObject(fullName: String): Optional<ByteArray>

    private fun hashName(name: String) = (UUID(System.currentTimeMillis(), Random().nextLong()).toString()
            + name.substring(if (name.contains(".")) name.lastIndexOf('.') else 0))
}
