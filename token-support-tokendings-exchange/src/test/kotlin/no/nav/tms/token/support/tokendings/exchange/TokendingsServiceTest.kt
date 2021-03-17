package no.nav.tms.token.support.tokendings.exchange

import com.nimbusds.jwt.SignedJWT
import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.tokendings.exchange.consumer.TokendingsConsumer
import no.nav.tms.token.support.tokendings.exchange.service.CachingTokendingsService
import no.nav.tms.token.support.tokendings.exchange.service.NonCachingTokendingsService
import no.nav.tms.token.support.tokendings.exchange.service.TokenStringUtil
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class TokendingsServiceTest {

    private val tokendingsConsumer = mockk<TokendingsConsumer>()
    private val jwtAudience = "https://tokendings.url/token"
    private val clientId = "cluster:namespace:thisApi"
    private val privateJwk = JwkBuilder.generateJwk()

    private val nonCachingtokendingsService = NonCachingTokendingsService(tokendingsConsumer, jwtAudience, clientId, privateJwk)
    private val cachingTokendingsService = CachingTokendingsService(tokendingsConsumer, jwtAudience, clientId, privateJwk, 10, 5)

    @AfterEach
    fun cleanup() {
        clearMocks(tokendingsConsumer)
        unmockkObject(TokenStringUtil)

    }


    @Test
    fun `Non-caching service should sign a jwt with correct claims and retrieve token from response`() {
        val assertion = slot<String>()
        val token = "<token>"
        val exchangedToken = "<exchanged token>"
        val target = "cluster:namespace:otherApi"

       coEvery {
           tokendingsConsumer.exchangeToken(any(), capture(assertion), target)
       } returns TokendingsResponseObjectMother.createTokendingsResponse(exchangedToken)

        val result = runBlocking {
            nonCachingtokendingsService.exchangeToken(token, target)
        }

        result `should be equal to` exchangedToken

        val signedJwt = assertion.captured.let { SignedJWT.parse(it) }
        val claims = signedJwt.jwtClaimsSet

        claims.audience `should contain` jwtAudience
        claims.issuer `should be equal to` clientId
        claims.subject `should be equal to` clientId
    }

    @Test
    fun `Caching service should sign a jwt with correct claims and retrieve token from response`() {
        val assertion = slot<String>()
        val token = "<token>"
        val subject = "<subject>"
        val exchangedToken = "<exchanged token>"
        val target = "cluster:namespace:otherApi"

        mockkObject(TokenStringUtil)

        every {
            TokenStringUtil.extractSubject(token)
        } returns subject

        coEvery {
            tokendingsConsumer.exchangeToken(any(), capture(assertion), target)
        } returns TokendingsResponseObjectMother.createTokendingsResponse(exchangedToken)

        val result = runBlocking {
            cachingTokendingsService.exchangeToken(token, target)
        }

        result `should be equal to` exchangedToken

        val signedJwt = assertion.captured.let { SignedJWT.parse(it) }
        val claims = signedJwt.jwtClaimsSet

        claims.audience `should contain` jwtAudience
        claims.issuer `should be equal to` clientId
        claims.subject `should be equal to` clientId
    }

    @Test
    fun `CachingService should not make additional external calls while token is not yet expired`() {
        val assertion = slot<String>()
        val token = "<token>"
        val subject = "<subject>"
        val exchangedToken = "<exchanged token>"
        val target = "cluster:namespace:otherApi"

        mockkObject(TokenStringUtil)

        every {
            TokenStringUtil.extractSubject(token)
        } returns subject

        coEvery {
            tokendingsConsumer.exchangeToken(any(), capture(assertion), target)
        } returns TokendingsResponseObjectMother.createTokendingsResponse(exchangedToken)

        runBlocking {
            cachingTokendingsService.exchangeToken(token, target)
            cachingTokendingsService.exchangeToken(token, target)
            cachingTokendingsService.exchangeToken(token, target)
        }

        coVerify(exactly = 1) {tokendingsConsumer.exchangeToken(any(), any(), target) }
    }

    @Test
    fun `CachingService should make external calls when access token is missing or expired`() {
        val assertion = slot<String>()
        val token = "<token>"
        val subject = "<subject>"
        val exchangedToken = "<exchanged token>"
        val target = "cluster:namespace:otherApi"

        mockkObject(TokenStringUtil)

        every {
            TokenStringUtil.extractSubject(token)
        } returns subject

        coEvery {
            tokendingsConsumer.exchangeToken(any(), capture(assertion), target)
        } returns TokendingsResponseObjectMother.createTokendingsResponse(exchangedToken, expiresIn = 0)

        runBlocking {
            cachingTokendingsService.exchangeToken(token, target)
            cachingTokendingsService.exchangeToken(token, target)
            cachingTokendingsService.exchangeToken(token, target)
        }

        coVerify(exactly = 3) {tokendingsConsumer.exchangeToken(any(), any(), target) }
    }
}
