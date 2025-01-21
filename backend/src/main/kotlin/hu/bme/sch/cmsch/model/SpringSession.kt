package hu.bme.sch.cmsch.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Entity
@Table(name = "SPRING_SESSION")
open class SpringSession {
    @Id
    @Size(max = 36)
    @Column(name = "PRIMARY_ID", nullable = false, length = 36)
    open var primaryId: String? = null

    @Size(max = 36)
    @NotNull
    @Column(name = "SESSION_ID", nullable = false, length = 36)
    open var sessionId: String? = null

    @NotNull
    @Column(name = "CREATION_TIME", nullable = false)
    open var creationTime: Long? = null

    @NotNull
    @Column(name = "LAST_ACCESS_TIME", nullable = false)
    open var lastAccessTime: Long? = null

    @NotNull
    @Column(name = "MAX_INACTIVE_INTERVAL", nullable = false)
    open var maxInactiveInterval: Int? = null

    @NotNull
    @Column(name = "EXPIRY_TIME", nullable = false)
    open var expiryTime: Long? = null

    @Size(max = 100)
    @Column(name = "PRINCIPAL_NAME", length = 100)
    open var principalName: String? = null
}
