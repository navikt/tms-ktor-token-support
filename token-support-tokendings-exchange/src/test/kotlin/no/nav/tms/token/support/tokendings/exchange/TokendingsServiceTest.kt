package no.nav.tms.token.support.tokendings.exchange

import com.nimbusds.jwt.SignedJWT
import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.tokendings.exchange.config.cache.AccessTokenKey
import no.nav.tms.token.support.tokendings.exchange.consumer.TokendingsConsumer
import no.nav.tms.token.support.tokendings.exchange.service.CachingTokendingsService
import no.nav.tms.token.support.tokendings.exchange.service.NonCachingTokendingsService
import no.nav.tms.token.support.tokendings.exchange.service.TokenStringUtil
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not be equal to`
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
            TokenStringUtil.createCacheKey(token, target)
        } returns AccessTokenKey(subject, "Level3", target)

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
            TokenStringUtil.createCacheKey(token, target)
        } returns AccessTokenKey(subject, "Level3", target)

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
            TokenStringUtil.createCacheKey(token, target)
        } returns AccessTokenKey(subject, "Level3", target)

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

    @Test
    fun `CachingService should cache one unique token per target`() {
        val assertion = slot<String>()
        val token = "<token>"
        val subject = "<subject>"
        val exchangedToken1 = "<exchanged token 1>"
        val exchangedToken2 = "<exchanged token 2>"
        val target1 = "cluster:namespace:otherApi1"
        val target2 = "cluster:namespace:otherApi2"

        mockkObject(TokenStringUtil)

        every {
            TokenStringUtil.createCacheKey(token, target1)
        } returns AccessTokenKey(subject, "Level3", target1)

        every {
            TokenStringUtil.createCacheKey(token, target2)
        } returns AccessTokenKey(subject, "Level3", target2)

        coEvery {
            tokendingsConsumer.exchangeToken(any(), capture(assertion), target1)
        } returns TokendingsResponseObjectMother.createTokendingsResponse(exchangedToken1)

        coEvery {
            tokendingsConsumer.exchangeToken(any(), capture(assertion), target2)
        } returns TokendingsResponseObjectMother.createTokendingsResponse(exchangedToken2)

        val result1 = runBlocking { cachingTokendingsService.exchangeToken(token, target1) }
        val result2 = runBlocking { cachingTokendingsService.exchangeToken(token, target2) }
        val result3 = runBlocking { cachingTokendingsService.exchangeToken(token, target1) }
        val result4 = runBlocking { cachingTokendingsService.exchangeToken(token, target2) }

        runBlocking { cachingTokendingsService.exchangeToken(token, target1) }
        runBlocking { cachingTokendingsService.exchangeToken(token, target2) }
        runBlocking { cachingTokendingsService.exchangeToken(token, target1) }
        runBlocking { cachingTokendingsService.exchangeToken(token, target2) }

        coVerify(exactly = 1) {tokendingsConsumer.exchangeToken(any(), any(), target1) }
        coVerify(exactly = 1) {tokendingsConsumer.exchangeToken(any(), any(), target2) }

        result1 `should be equal to` result3
        result2 `should be equal to` result4
        result1 `should not be equal to` result2
        result3 `should not be equal to` result4
    }

    @Test
    fun `CachingService should cache one unique token per security level`() {
        val assertion = slot<String>()
        val token1 = "<token1>"
        val token2 = "<token2>"
        val subject = "<subject>"
        val exchangedToken1 = "<exchanged token 1>"
        val exchangedToken2 = "<exchanged token 2>"
        val target = "cluster:namespace:otherApi"
        val level3 = "Level3"
        val level4 = "Level4"

        mockkObject(TokenStringUtil)

        every {
            TokenStringUtil.createCacheKey(token1, target)
        } returns AccessTokenKey(subject, level3, target)

        every {
            TokenStringUtil.createCacheKey(token2, target)
        } returns AccessTokenKey(subject, level4, target)

        coEvery {
            tokendingsConsumer.exchangeToken(token1, capture(assertion), target)
        } returns TokendingsResponseObjectMother.createTokendingsResponse(exchangedToken1)

        coEvery {
            tokendingsConsumer.exchangeToken(token2, capture(assertion), target)
        } returns TokendingsResponseObjectMother.createTokendingsResponse(exchangedToken2)

        val result1 = runBlocking { cachingTokendingsService.exchangeToken(token1, target) }
        val result2 = runBlocking { cachingTokendingsService.exchangeToken(token2, target) }
        val result3 = runBlocking { cachingTokendingsService.exchangeToken(token1, target) }
        val result4 = runBlocking { cachingTokendingsService.exchangeToken(token2, target) }

        runBlocking { cachingTokendingsService.exchangeToken(token1, target) }
        runBlocking { cachingTokendingsService.exchangeToken(token2, target) }
        runBlocking { cachingTokendingsService.exchangeToken(token1, target) }
        runBlocking { cachingTokendingsService.exchangeToken(token2, target) }

        coVerify(exactly = 1) {tokendingsConsumer.exchangeToken(token1, any(), target) }
        coVerify(exactly = 1) {tokendingsConsumer.exchangeToken(token2, any(), target) }

        result1 `should be equal to` result3
        result2 `should be equal to` result4
        result1 `should not be equal to` result2
        result3 `should not be equal to` result4
    }
}
