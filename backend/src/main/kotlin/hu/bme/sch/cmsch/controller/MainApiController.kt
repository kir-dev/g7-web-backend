package hu.bme.sch.cmsch.controller

import hu.bme.sch.cmsch.model.UserEntity
import hu.bme.sch.cmsch.service.TimeService
import org.springframework.boot.info.BuildProperties
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat

val UNKNOWN_USER = UserEntity(0, fullName = "Feature Not Available")

@RestController
@RequestMapping("/api")
class MainApiController(
    private val buildProperties: BuildProperties,
    private val clock: TimeService
) {

    private val formatter = SimpleDateFormat("yyyy.MM.dd. HH:mm:ss")

    @ResponseBody
    @GetMapping("/version")
    fun version(): String = buildProperties.version!!

    @ResponseBody
    @GetMapping("/time")
    fun time(): String = formatter.format(clock.getTimeInSeconds() * 1000)

}
