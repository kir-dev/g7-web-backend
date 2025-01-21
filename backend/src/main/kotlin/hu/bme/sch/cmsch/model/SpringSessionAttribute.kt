package hu.bme.sch.cmsch.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "SPRING_SESSION_ATTRIBUTES")
open class SpringSessionAttribute {
    @EmbeddedId
    open var id: SpringSessionAttributeId? = null

    @MapsId("sessionPrimaryId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "SESSION_PRIMARY_ID", nullable = false)
    open var sessionPrimary: SpringSession? = null

    @NotNull
    @Column(name = "ATTRIBUTE_BYTES", nullable = false, length = Integer.MAX_VALUE)
    open var attributeBytes: ByteArray? = null
}
