package hu.bme.sch.cmsch.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import hu.bme.sch.cmsch.config.StartupPropertyConfig
import hu.bme.sch.cmsch.model.UserEntity
import hu.bme.sch.cmsch.util.sha256
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import jakarta.annotation.PostConstruct
import java.io.ByteArrayOutputStream


@Service
class UserProfileGeneratorService(
    private val startupPropertyConfig: StartupPropertyConfig,
    private val storageService: StorageService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        File(startupPropertyConfig.profileGenerationTarget).mkdirs()
    }

    @Throws(WriterException::class, IOException::class)
    private fun createQR(user: UserEntity) {
        val format = "png"
        val path = "profiles"
        val fileName =  "${user.cmschId}.$format"
        if (storageService.exists(path, fileName)) {
            log.info("QR code already exists for user ${user.fullName}")
            return
        }

        val matrix = MultiFormatWriter().encode(
                String(user.cmschId.toByteArray(StandardCharsets.UTF_8), StandardCharsets.UTF_8),
                BarcodeFormat.QR_CODE,
                startupPropertyConfig.profileQrCodeSize, startupPropertyConfig.profileQrCodeSize,
                mutableMapOf(
                        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                        EncodeHintType.CHARACTER_SET to StandardCharsets.UTF_8.toString(),
                        EncodeHintType.MARGIN to 1
                )
        )

        val qrData = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(matrix, format, qrData)
        storageService.saveNamedObject(path, fileName, qrData.toByteArray())

        log.info("New QR code was generated to /cdn/$path/$fileName for user ${user.fullName}")
    }

    fun generateFullProfileForUser(user: UserEntity) {
        generateProfileIdForUser(user)
        createQR(user)
    }

    fun generateProfileIdForUser(user: UserEntity) {
        user.cmschId = (startupPropertyConfig.profileQrPrefix + (user.internalId + startupPropertyConfig.profileSalt)
            .sha256()
            .substring(startupPropertyConfig.profileQrPrefix.length, 40))
    }

}
