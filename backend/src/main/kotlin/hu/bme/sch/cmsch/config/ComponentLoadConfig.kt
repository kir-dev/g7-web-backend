package hu.bme.sch.cmsch.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("hu.bme.sch.cmsch.component.load")
data class ComponentLoadConfig @ConstructorBinding constructor(

    var admission: Boolean,
    var app: Boolean,
    var bmejegy: Boolean,
    var challenge: Boolean,
    var countdown: Boolean,
    var debt: Boolean,
    var event: Boolean,
    var staticPage: Boolean,
    var groupselection: Boolean,
    var home: Boolean,
    var impressum: Boolean,
    var leaderboard: Boolean,
    var location: Boolean,
    var login: Boolean,
    var news: Boolean,
    var profile: Boolean,
    var qrFight: Boolean,
    var race: Boolean,
    var riddle: Boolean,
    var form: Boolean,
    var task: Boolean,
    var team: Boolean,
    var token: Boolean,
    var communities: Boolean,

) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun onInit() {
        log.info("ComponentLoadConfig settings: {}", this.toString())
    }

}
