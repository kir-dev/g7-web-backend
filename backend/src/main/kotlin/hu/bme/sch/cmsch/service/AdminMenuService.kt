package hu.bme.sch.cmsch.service

import hu.bme.sch.cmsch.component.login.CmschUser
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import java.util.concurrent.ConcurrentHashMap

@Service
class AdminMenuService {

    private val entries: MutableMap<String, MutableList<AdminMenuEntry>> = ConcurrentHashMap()
    private val categories: MutableMap<String, AdminMenuCategory> = ConcurrentHashMap()

    fun registerEntry(component: String, entry: AdminMenuEntry) {
        entries.computeIfAbsent(component) { mutableListOf() }.add(entry)
    }

    fun registerCategory(component: String, category: AdminMenuCategory) {
        categories[component] = category
    }

    private fun getMenusOfCategory(category: String, user: CmschUser): List<AdminMenuEntry> {
        return entries.getOrDefault(category, listOf())
            .filter { it.showPermission.validate(user) }
            .sortedBy { it.priority }
    }

    fun addPartsForMenu(user: CmschUser, model: Model) {
        model.addAttribute("menu", categories.entries
            .associateWith { getMenusOfCategory(it.key, user) }
            .entries
            .toList()
            .sortedBy { it.key.value.priority })
    }

}

data class AdminMenuCategory(val title: String, val priority: Int)

data class AdminMenuEntry(
    val title: String,
    val icon: String,
    val target: String,
    val priority: Int,
    val showPermission: PermissionValidator
)
