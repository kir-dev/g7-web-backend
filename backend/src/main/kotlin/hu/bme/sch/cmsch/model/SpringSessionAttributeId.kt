package hu.bme.sch.cmsch.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

@Embeddable
open class SpringSessionAttributeId : Serializable {
    @Size(max = 36)
    @NotNull
    @Column(name = "SESSION_PRIMARY_ID", nullable = false, length = 36)
    open var sessionPrimaryId: String? = null

    @Size(max = 200)
    @NotNull
    @Column(name = "ATTRIBUTE_NAME", nullable = false, length = 200)
    open var attributeName: String? = null
    override fun hashCode(): Int = Objects.hash(sessionPrimaryId, attributeName)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as SpringSessionAttributeId

        return sessionPrimaryId == other.sessionPrimaryId &&
                attributeName == other.attributeName
    }

    companion object {
        private const val serialVersionUID = 0L
    }
}
