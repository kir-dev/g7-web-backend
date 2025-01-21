package hu.bme.sch.cmsch.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

/**
 * Created for JdbcOAuth2AuthorizedClientService
 */
@Entity
@Table(name = "OAUTH2_AUTHORIZED_CLIENT")
open class Oauth2AuthorizedClient {
    @EmbeddedId
    open var id: Oauth2AuthorizedClientId? = null

    @Size(max = 100)
    @NotNull
    @Column(name = "ACCESS_TOKEN_TYPE", nullable = false, length = 100)
    open var accessTokenType: String? = null

    @NotNull
    @Column(name = "ACCESS_TOKEN_ISSUED_AT", nullable = false)
    open var accessTokenIssuedAt: Instant? = null

    @NotNull
    @Column(name = "ACCESS_TOKEN_EXPIRES_AT", nullable = false)
    open var accessTokenExpiresAt: Instant? = null

    @Size(max = 1000)
    @ColumnDefault("NULL")
    @Column(name = "ACCESS_TOKEN_SCOPES", length = 1000)
    open var accessTokenScopes: String? = null

    @ColumnDefault("NULL")
    @Column(name = "REFRESH_TOKEN_ISSUED_AT")
    open var refreshTokenIssuedAt: Instant? = null

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATED_AT", nullable = false)
    open var createdAt: Instant? = null

    @Column(name = "ACCESS_TOKEN_VALUE", length = Integer.MAX_VALUE)
    open var accessTokenValue: ByteArray? = null

    @ColumnDefault("NULL")
    @Column(name = "REFRESH_TOKEN_VALUE", length = Integer.MAX_VALUE)
    open var refreshTokenValue: ByteArray? = null

}
