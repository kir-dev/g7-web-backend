package hu.bme.sch.cmsch.component.home

import hu.bme.sch.cmsch.component.*
import hu.bme.sch.cmsch.model.RoleType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(
    prefix = "hu.bme.sch.cmsch.component.load",
    name = ["home"],
    havingValue = "true",
    matchIfMissing = false
)
class HomeComponent(
    componentSettingService: ComponentSettingService,
    env: Environment
) : ComponentBase("home", "/home", componentSettingService, env) {

    final override val allSettings by lazy {
        listOf(
            title, menuDisplayName, minRole,

            displayGroup,
            welcomeMessage,
            content
        )
    }

    final val title = SettingProxy(componentSettingService, component,
        "title", "Kezdőlap",
        fieldName = "Lap címe", description = "Ez jelenik meg a böngésző címsorában"
    )

    final override val menuDisplayName = SettingProxy(componentSettingService, component,
        "menuDisplayName", "Kezdőlap", serverSideOnly = true,
        fieldName = "Menü neve", description = "Ez lesz a neve a menünek"
    )

    final override val minRole = MinRoleSettingProxy(componentSettingService, component,
        "minRole", "", minRoleToEdit = RoleType.NOBODY,
        fieldName = "Jogosultságok", description = "Melyik roleokkal nyitható meg az oldal"
    )

    /// -------------------------------------------------------------------------------------------------------------------

    val displayGroup = SettingProxy(componentSettingService, component,
        "displayGroup", "", type = SettingType.COMPONENT_GROUP, persist = false,
        fieldName = "Megjelenés",
        description = "A kezdőlap megjelenése"
    )

    val welcomeMessage = SettingProxy(componentSettingService, component,
        "welcomeMessage", "Üdvözlünk a {} portálon", type = SettingType.TEXT,
        fieldName = "Üdvözlő üzenet ", description = "Ha üres akkor nincs, a {} pedig ki van cserélve az oldal nevére"
    )

    val content = SettingProxy(componentSettingService, component,
        "content", "",
        type = SettingType.LONG_TEXT_MARKDOWN,
        fieldName = "Megjelenő szöveg", description = "A kezdőlapon megjelenő szöveg. Ha üres akkor nincs ilyen."
    )

}
