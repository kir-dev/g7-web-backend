package hu.bme.sch.cmsch.component.news

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@ConditionalOnBean(NewsComponent::class)
interface NewsRepository : CrudRepository<NewsEntity, Int> {
    fun findTop4ByVisibleTrueOrderByTimestampDesc(): List<NewsEntity>
    fun findAllByVisibleTrueOrderByTimestampDesc(): List<NewsEntity>
    fun findByUrlAndVisibleTrue(url: String): Optional<NewsEntity>
    fun findByUrl(url: String): Optional<NewsEntity>
}
