package no.nav.tms.token.support.tokenx.validation.user

import com.auth0.jwt.interfaces.DecodedJWT
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance
import java.time.Instant
import java.time.temporal.ChronoUnit

data class TokenXUser (
    val ident: String,
    val levelOfAssurance: LevelOfAssurance,
    val tokenExpirationTime: Instant,
    val jwt: DecodedJWT
) {
    val tokenString get() = jwt.token

    val tokenClaims get() = jwt.claims

    fun createAuthenticationHeader(): String {
        return "Bearer $tokenString"
    }

    override fun toString(): String {
        return "AuthenticatedUser(ident='***', levelOfAssurance=${levelOfAssurance.name}, token='***', expiryTime=$tokenExpirationTime)"
    }

    fun isTokenExpired(): Boolean {
        val now = Instant.now()
        return tokenExpirationTime.isBefore(now)
    }

    fun isTokenAboutToExpire(expiryThresholdInMinutes: Long): Boolean {
        val now = Instant.now()
        return now.isAfter(tokenExpirationTime.minus(expiryThresholdInMinutes, ChronoUnit.MINUTES))
    }
}
