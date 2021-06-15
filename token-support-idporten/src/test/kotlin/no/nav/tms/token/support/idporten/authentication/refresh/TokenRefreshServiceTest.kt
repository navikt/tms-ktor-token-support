package no.nav.tms.token.support.idporten.authentication.refresh

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.mockk.mockk
import no.nav.tms.token.support.idporten.JwkBuilder
import no.nav.tms.token.support.idporten.JwtBuilder
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES
import java.util.*

internal class TokenRefreshServiceTest {
    private val privateJwk = JwkBuilder.generateJwk()

    private val tokenRefreshConsumer: TokenRefreshConsumer = mockk()


    @Test
    fun `Should correctly decide if a token is within a given percent margin of expiry time relative to issue time`() {
        val refreshMarginPercentage = 25

        val tokenRefreshService = TokenRefreshService(tokenRefreshConsumer, refreshMarginPercentage)

        val token1 = createTokenWithOffsetIssExpTimes(0, 60)
        val token2 = createTokenWithOffsetIssExpTimes(10, 50)
        val token3 = createTokenWithOffsetIssExpTimes(20, 40)
        val token4 = createTokenWithOffsetIssExpTimes(30, 30)
        val token5 = createTokenWithOffsetIssExpTimes(40, 20)
        val token6 = createTokenWithOffsetIssExpTimes(50, 10)
        val token7 = createTokenWithOffsetIssExpTimes(60, 0)

        tokenRefreshService.shouldRefreshToken(token1) `should be equal to` false
        tokenRefreshService.shouldRefreshToken(token2) `should be equal to` false
        tokenRefreshService.shouldRefreshToken(token3) `should be equal to` false
        tokenRefreshService.shouldRefreshToken(token4) `should be equal to` false
        tokenRefreshService.shouldRefreshToken(token5) `should be equal to` false
        tokenRefreshService.shouldRefreshToken(token6) `should be equal to` true
        tokenRefreshService.shouldRefreshToken(token7) `should be equal to` true
    }

    private fun createTokenWithOffsetIssExpTimes(minutesSinceIssueTime: Long, minutesToExpiryTime: Long): DecodedJWT {
        val now = Instant.now()

        val issueTime = now.minus(minutesSinceIssueTime, MINUTES).toDate()
        val expiryTime  = now.plus(minutesToExpiryTime, MINUTES).toDate()

        val jwtString = JwtBuilder.generateJwtString(issueTime, expiryTime, privateJwk)

        return JWT.decode(jwtString)
    }

    private fun Instant.toDate() = Date.from(this)
}
