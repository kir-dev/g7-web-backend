package hu.bme.sch.cmsch.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

/**
 * Created for JdbcOAuth2AuthorizedClientService
 */
@Embeddable
open class Oauth2AuthorizedClientId : Serializable {
    @Size(max = 100)
    @NotNull
    @Column(name = "CLIENT_REGISTRATION_ID", nullable = false, length = 100)
    open var clientRegistrationId: String? = null

    @Size(max = 200)
    @NotNull
    @Column(name = "PRINCIPAL_NAME", nullable = false, length = 200)
    open var principalName: String? = null
    override fun hashCode(): Int = Objects.hash(clientRegistrationId, principalName)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as Oauth2AuthorizedClientId

        return clientRegistrationId == other.clientRegistrationId &&
                principalName == other.principalName
    }

    companion object {
        private const val serialVersionUID = 0L
    }
}
