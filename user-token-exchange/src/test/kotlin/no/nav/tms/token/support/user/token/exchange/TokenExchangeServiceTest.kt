package no.nav.tms.token.support.user.token.exchange

import com.nimbusds.jwt.SignedJWT
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.user.token.exchange.service.AccessTokenKey
import no.nav.tms.token.support.user.token.exchange.service.CachingExchangeService
import no.nav.tms.token.support.user.token.exchange.service.NonCachingExchangeService
import no.nav.tms.token.support.user.token.exchange.service.TokenExchangeConsumer
import no.nav.tms.token.support.user.token.exchange.service.TokenExchangeResponse
import no.nav.tms.token.support.user.token.exchange.service.TokenStringUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.SocketTimeoutException

internal class TokenExchangeServiceTest {

    private val tokenExchangeConsumer = mockk<TokenExchangeConsumer>()
    private val jwtAudience = "https://token-exchange.url/token"
    private val clientId = "cluster:namespace:thisApi"
    private val privateJwk = JwkBuilder.generateJwk()

    private val nonCachingTokenExchangeService =
        NonCachingExchangeService(tokenExchangeConsumer, jwtAudience, clientId, privateJwk)
    private val cachingTokenexchangeService =
        CachingExchangeService(tokenExchangeConsumer, jwtAudience, clientId, privateJwk, 10, 5)

    @AfterEach
    fun cleanup() {
        clearMocks(tokenExchangeConsumer)
        unmockkObject(TokenStringUtil)
    }


    @Test
    fun `Non-caching service should sign a jwt with correct claims and retrieve token from response`() {
        val assertion = slot<String>()
        val token = "<token>"
        val exchangedToken = "<exchanged token>"
        val target = "cluster:namespace:otherApi"

        coEvery {
            tokenExchangeConsumer.exchangeToken(any(), capture(assertion), target)
        } returns createTokenExchangeResponse(exchangedToken)

        val result = runBlocking {
            nonCachingTokenExchangeService.exchangeToken(token, target)
        }

        result shouldBe exchangedToken

        val signedJwt = assertion.captured.let { SignedJWT.parse(it) }
        val claims = signedJwt.jwtClaimsSet

        claims.audience shouldContain jwtAudience
        claims.issuer shouldBe clientId
        claims.subject shouldBe clientId
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
        } returns AccessTokenKey(subject, "idporten-loa-substantial", target)

        coEvery {
            tokenExchangeConsumer.exchangeToken(any(), capture(assertion), target)
        } returns createTokenExchangeResponse(exchangedToken)

        val result = runBlocking {
            cachingTokenexchangeService.exchangeToken(token, target)
        }

        result shouldBe exchangedToken

        val signedJwt = assertion.captured.let { SignedJWT.parse(it) }
        val claims = signedJwt.jwtClaimsSet

        claims.audience shouldContain jwtAudience
        claims.issuer shouldBe clientId
        claims.subject shouldBe clientId
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
        } returns AccessTokenKey(subject, "idporten-loa-substantial", target)

        coEvery {
            tokenExchangeConsumer.exchangeToken(any(), capture(assertion), target)
        } returns createTokenExchangeResponse(exchangedToken)

        runBlocking {
            cachingTokenexchangeService.exchangeToken(token, target)
            cachingTokenexchangeService.exchangeToken(token, target)
            cachingTokenexchangeService.exchangeToken(token, target)
        }

        coVerify(exactly = 1) { tokenExchangeConsumer.exchangeToken(any(), any(), target) }
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
        } returns AccessTokenKey(subject, "idporten-loa-substantial", target)

        coEvery {
            tokenExchangeConsumer.exchangeToken(any(), capture(assertion), target)
        } returns createTokenExchangeResponse(exchangedToken, expiresIn = 0)

        runBlocking {
            cachingTokenexchangeService.exchangeToken(token, target)
            cachingTokenexchangeService.exchangeToken(token, target)
            cachingTokenexchangeService.exchangeToken(token, target)
        }

        coVerify(exactly = 3) { tokenExchangeConsumer.exchangeToken(any(), any(), target) }
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
        } returns AccessTokenKey(subject, "idporten-loa-substantial", target1)

        every {
            TokenStringUtil.createCacheKey(token, target2)
        } returns AccessTokenKey(subject, "idporten-loa-substantial", target2)

        coEvery {
            tokenExchangeConsumer.exchangeToken(any(), capture(assertion), target1)
        } returns createTokenExchangeResponse(exchangedToken1)

        coEvery {
            tokenExchangeConsumer.exchangeToken(any(), capture(assertion), target2)
        } returns createTokenExchangeResponse(exchangedToken2)

        val result1 = runBlocking { cachingTokenexchangeService.exchangeToken(token, target1) }
        val result2 = runBlocking { cachingTokenexchangeService.exchangeToken(token, target2) }
        val result3 = runBlocking { cachingTokenexchangeService.exchangeToken(token, target1) }
        val result4 = runBlocking { cachingTokenexchangeService.exchangeToken(token, target2) }

        runBlocking { cachingTokenexchangeService.exchangeToken(token, target1) }
        runBlocking { cachingTokenexchangeService.exchangeToken(token, target2) }
        runBlocking { cachingTokenexchangeService.exchangeToken(token, target1) }
        runBlocking { cachingTokenexchangeService.exchangeToken(token, target2) }

        coVerify(exactly = 1) { tokenExchangeConsumer.exchangeToken(any(), any(), target1) }
        coVerify(exactly = 1) { tokenExchangeConsumer.exchangeToken(any(), any(), target2) }

        result1 shouldBe result3
        result2 shouldBe result4
        result1 shouldNotBe result2
        result3 shouldNotBe result4
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
        val substantial = "idporten-loa-substantial"
        val high = "idporten-loa-high"

        mockkObject(TokenStringUtil)

        every {
            TokenStringUtil.createCacheKey(token1, target)
        } returns AccessTokenKey(subject, substantial, target)

        every {
            TokenStringUtil.createCacheKey(token2, target)
        } returns AccessTokenKey(subject, high, target)

        coEvery {
            tokenExchangeConsumer.exchangeToken(token1, capture(assertion), target)
        } returns createTokenExchangeResponse(exchangedToken1)

        coEvery {
            tokenExchangeConsumer.exchangeToken(token2, capture(assertion), target)
        } returns createTokenExchangeResponse(exchangedToken2)

        val result1 = runBlocking { cachingTokenexchangeService.exchangeToken(token1, target) }
        val result2 = runBlocking { cachingTokenexchangeService.exchangeToken(token2, target) }
        val result3 = runBlocking { cachingTokenexchangeService.exchangeToken(token1, target) }
        val result4 = runBlocking { cachingTokenexchangeService.exchangeToken(token2, target) }

        runBlocking { cachingTokenexchangeService.exchangeToken(token1, target) }
        runBlocking { cachingTokenexchangeService.exchangeToken(token2, target) }
        runBlocking { cachingTokenexchangeService.exchangeToken(token1, target) }
        runBlocking { cachingTokenexchangeService.exchangeToken(token2, target) }

        coVerify(exactly = 1) { tokenExchangeConsumer.exchangeToken(token1, any(), target) }
        coVerify(exactly = 1) { tokenExchangeConsumer.exchangeToken(token2, any(), target) }

        result1 shouldBe result3
        result2 shouldBe result4
        result1 shouldNotBe result2
        result3 shouldNotBe result4
    }

    @Test
    fun `Should throw UserTokenExchangeException if exchangeprocess fails`() {
        assertNonCachingServiceThrows { IllegalArgumentException() }
        assertNonCachingServiceThrows { SocketTimeoutException() }
        assertNonCachingServiceThrows { Exception() }
        assertCachingServiceThrows { IllegalArgumentException() }
        assertCachingServiceThrows { SocketTimeoutException() }
        assertCachingServiceThrows { Exception() }

    }

    private fun assertNonCachingServiceThrows( throwable: () -> Throwable) = run {
        NonCachingExchangeService(
            tokenExchangeConsumer = mockk<TokenExchangeConsumer>().apply {
                coEvery { exchangeToken(any(), any(), any()) } throws throwable()
            },
            jwtAudience = "some:aud",
            clientId = "some:client",
            privateJwk = privateJwk
        ).apply {
            assertThrows<UserTokenExchangeException> { runBlocking { exchangeToken("token", "token") } }
        }
    }

    private fun assertCachingServiceThrows( throwable: () -> Throwable) = run {
        CachingExchangeService(
            tokenExchangeConsumer = mockk<TokenExchangeConsumer>().apply {
                coEvery { exchangeToken(any(), any(), any()) } throws throwable()
            },
            jwtAudience = "some:aud",
            clientId = "some:client",
            privateJwk = privateJwk,
            maxCacheEntries = 1,
            cacheExpiryMarginSeconds = 6
        ).apply {
            assertThrows<UserTokenExchangeException> { runBlocking { exchangeToken("token", "token") } }
        }
    }

    fun createTokenExchangeResponse(accessToken: String, expiresIn: Int = 300) = TokenExchangeResponse (
        accessToken = accessToken,
        tokenType = "",
        issuedTokenType = "",
        expiresIn = expiresIn
    )
}

