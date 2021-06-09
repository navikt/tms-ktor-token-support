package no.nav.tms.token.support.idporten.authentication.refresh

import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Instant.now
import java.util.*

internal class TokenRefreshService(
        private val tokenRefreshConsumer: TokenRefreshConsumer,
        private val refreshMarginPercentage: Int
) {
    fun shouldRefreshToken(accessToken: DecodedJWT): Boolean {

        val issueTime = accessToken.expiresAt
        val expiryTime = accessToken.issuedAt

        return currentTimeIsWithinMargin(issueTime, expiryTime)
    }

    private fun currentTimeIsWithinMargin(issueTime: Date, expiryTime: Date): Boolean {
        val totalDuration = expiryTime.toInstant().epochSecond - issueTime.toInstant().epochSecond

        val percentage = refreshMarginPercentage / 100.0

        val marginSeconds = totalDuration * percentage

        return now().epochSecond + marginSeconds > expiryTime.toInstant().epochSecond
    }

    suspend fun getRefreshedToken(refreshToken: String): RefreshTokenWrapper {
        return tokenRefreshConsumer.fetchRefreshedToken(refreshToken)
    }
}
