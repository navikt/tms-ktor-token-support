package no.nav.tms.token.support.tokendings.exchange

import com.nimbusds.jwt.SignedJWT
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.tokendings.exchange.consumer.TokendingsConsumer
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.Test

internal class TokendingsServiceTest {

    private val tokendingsConsumer = mockk<TokendingsConsumer>()
    private val jwtAudience = "https://tokendings.url/token"
    private val clientId = "cluster:namespace:thisApi"
    private val privateJwk = JwkBuilder.generateJwk()

    private val tokendingsService = TokendingsService(tokendingsConsumer, jwtAudience, clientId, privateJwk)

    @Test
    fun `Should sign a jwt with correct claims and retrieve token from response`() {
        val assertion = slot<String>()
        val token = "<token>"
        val exchangedToken = "<exchanged token>"
        val target = "cluster:namespace:otherApi"

       coEvery {
           tokendingsConsumer.exchangeToken(any(), capture(assertion), target)
       } returns TokendingsResponseObjectMother.createTokendingsResponse(exchangedToken)

        val result = runBlocking {
            tokendingsService.exchangeToken(token, target)
        }

        result `should be equal to` exchangedToken

        val signedJwt = assertion.captured.let { SignedJWT.parse(it) }
        val claims = signedJwt.jwtClaimsSet

        claims.audience `should contain` jwtAudience
        claims.issuer `should be equal to` clientId
        claims.subject `should be equal to` clientId
    }
}
