package hu.bme.sch.cmsch.component.race

import hu.bme.sch.cmsch.repository.EntityPageDataSource
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnBean(RaceComponent::class)
interface FreestyleRaceRecordRepository : CrudRepository<FreestyleRaceRecordEntity, Int>,
    EntityPageDataSource<FreestyleRaceRecordEntity, Int> {

    override fun findAll(): List<FreestyleRaceRecordEntity>

}
