package no.nav.tms.token.support.idporten.authentication.refresh

import com.auth0.jwt.JWT
import java.time.Instant.now
import java.util.*

internal class TokenRefreshService(
        private val tokenRefreshConsumer: TokenRefreshConsumer,
        private val refreshMarginSeconds: Long
) {
    fun shouldRefreshToken(token: String): Boolean {
        val decodedJWT = JWT.decode(token)

        val expiryTime = decodedJWT.expiresAt

        return currentTImeIsWithinMargin(expiryTime)
    }

    private fun currentTImeIsWithinMargin(expiryTime: Date): Boolean {
        return now().epochSecond + refreshMarginSeconds > expiryTime.toInstant().epochSecond
    }

    suspend fun getRefreshedToken(refreshToken: String): String {
        return tokenRefreshConsumer.fetchRefreshedToken(refreshToken)
    }
}
