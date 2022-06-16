package hu.bme.sch.cmsch.component.opengraph

import hu.bme.sch.cmsch.component.event.EventRepository
import hu.bme.sch.cmsch.component.extrapage.ExtraPageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
open class OpenGraphService(
    private val extraPageRepository: Optional<ExtraPageRepository>,
    private val eventRepository: Optional<EventRepository>
) {

    @Transactional(readOnly = true)
    open fun findExtraPage(url: String): Optional<OpenGraphResource> {
        return extraPageRepository.flatMap { it.findByUrl(url) }
    }

    @Transactional(readOnly = true)
    open fun findEvent(url: String): Optional<OpenGraphResource> {
        return eventRepository.flatMap { it.findByUrl(url) }
    }

}
