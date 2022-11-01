package hu.bme.sch.cmsch.component.bmejegy

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnBean(BmejegyComponent::class)
interface BmejegyRecordRepository : CrudRepository<BmejegyRecordEntity, Int> {

    override fun findAll(): List<BmejegyRecordEntity>

    fun findAllByQrCode(qr: String): List<BmejegyRecordEntity>

    fun findAllByMatchedUserId(id: Int): List<BmejegyRecordEntity>

}
