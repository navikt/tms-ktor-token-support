package no.nav.tms.token.support.idporten.user

import java.time.Instant
import java.time.temporal.ChronoUnit

data class IdportenUser (
    val ident: String,
    val loginLevel: Int,
    val token: String,
    val tokenExpirationTime: Instant,
    val auxiliaryEssoToken: String? = null
) {

    fun createAuthenticationHeader(): String {
        return "Bearer $token"
    }

    override fun toString(): String {
        return "AuthenticatedUser(ident='***', loginLevel=$loginLevel, token='***', expiryTime=$tokenExpirationTime)"
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
