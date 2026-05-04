package no.nav.tms.token.support.entraid.token.fetcher

import com.nimbusds.jwt.SignedJWT
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.entraid.token.fetcher.impl.CachingEntraIdTokenFetcher
import no.nav.tms.token.support.entraid.token.fetcher.impl.ClientAssertionService
import no.nav.tms.token.support.entraid.token.fetcher.impl.EntraIdTokenException
import no.nav.tms.token.support.entraid.token.fetcher.impl.NonCachingEntraIdTokenFetcher
import no.nav.tms.token.support.entraid.token.fetcher.impl.TokenIssuerConsumer
import no.nav.tms.token.support.entraid.token.fetcher.impl.TokenResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.SocketTimeoutException

internal class EntraIdTokenFetcherTest {

    private val tokenIssuerConsumer: TokenIssuerConsumer = mockk()
    private val jwtAudience = "https://azure/token"
    private val clientId = "cluster.namespace.thisApi"
    private val privateJwk = JwkBuilder.generateJwk()

    private val clientAssertionService = ClientAssertionService(clientId, jwtAudience, privateJwk)

    private val nonCachingTokenFetcher = NonCachingEntraIdTokenFetcher(tokenIssuerConsumer, clientAssertionService)
    private val cachingTokenFetcher = CachingEntraIdTokenFetcher(tokenIssuerConsumer, clientAssertionService, 10, 5)

    @AfterEach
    fun cleanup() {
        clearMocks(tokenIssuerConsumer)
    }

    @Test
    fun `Non-caching fetcher should sign a jwt with correct claims and retrieve token from response`() {
        val assertion = slot<String>()
        val exchangedToken = "<exchanged token>"
        val target = "cluster.namespace.otherApi"

        coEvery {
            tokenIssuerConsumer.fetchToken(capture(assertion), target)
        } returns tokenResponse(exchangedToken)

        val result = runBlocking {
            nonCachingTokenFetcher.getAccessToken(target)
        }

        result shouldBe exchangedToken

        val signedJwt = assertion.captured.let { SignedJWT.parse(it) }
        val claims = signedJwt.jwtClaimsSet

        claims.audience shouldContain jwtAudience
        claims.issuer shouldBe clientId
        claims.subject shouldBe clientId
    }

    @Test
    fun `Caching fetcher should sign a jwt with correct claims and retrieve token from response`() {
        val assertion = slot<String>()
        val exchangedToken = "<exchanged token>"
        val target = "cluster.namespace.otherApi"

        coEvery {
            tokenIssuerConsumer.fetchToken(capture(assertion), target)
        } returns tokenResponse(exchangedToken)

        val result = runBlocking {
            cachingTokenFetcher.getAccessToken(target)
        }

        result shouldBe exchangedToken

        val signedJwt = assertion.captured.let { SignedJWT.parse(it) }
        val claims = signedJwt.jwtClaimsSet

        claims.audience shouldContain jwtAudience
        claims.issuer shouldBe clientId
        claims.subject shouldBe clientId
    }

    @Test
    fun `Caching fetcher should not make additional external calls while token is not yet expired`() {
        val assertion = slot<String>()
        val exchangedToken = "<exchanged token>"
        val target = "cluster.namespace.otherApi"

        coEvery {
            tokenIssuerConsumer.fetchToken(capture(assertion), target)
        } returns tokenResponse(exchangedToken)

        runBlocking {
            cachingTokenFetcher.getAccessToken(target)
            cachingTokenFetcher.getAccessToken(target)
            cachingTokenFetcher.getAccessToken(target)
        }

        coVerify(exactly = 1) { tokenIssuerConsumer.fetchToken(any(), target) }
    }

    @Test
    fun `Caching fetcher should make external calls when access token is missing or expired`() {
        val assertion = slot<String>()
        val exchangedToken = "<exchanged token>"
        val target = "cluster.namespace.otherApi"

        coEvery {
            tokenIssuerConsumer.fetchToken(capture(assertion), target)
        } returns tokenResponse(exchangedToken, expiresIn = 0)

        runBlocking {
            cachingTokenFetcher.getAccessToken(target)
            cachingTokenFetcher.getAccessToken(target)
            cachingTokenFetcher.getAccessToken(target)
        }

        coVerify(exactly = 3) { tokenIssuerConsumer.fetchToken(any(), target) }
    }

    @Test
    fun `Caching fetcher should cache one unique token per target`() {
        val assertion = slot<String>()
        val exchangedToken1 = "<exchanged token 1>"
        val exchangedToken2 = "<exchanged token 2>"
        val target1 = "cluster.namespace.otherApi1"
        val target2 = "cluster.namespace.otherApi2"

        coEvery {
            tokenIssuerConsumer.fetchToken(capture(assertion), target1)
        } returns tokenResponse(exchangedToken1)

        coEvery {
            tokenIssuerConsumer.fetchToken(capture(assertion), target2)
        } returns tokenResponse(exchangedToken2)

        val result1 = runBlocking { cachingTokenFetcher.getAccessToken(target1) }
        val result2 = runBlocking { cachingTokenFetcher.getAccessToken(target2) }
        val result3 = runBlocking { cachingTokenFetcher.getAccessToken(target1) }
        val result4 = runBlocking { cachingTokenFetcher.getAccessToken(target2) }

        coVerify(exactly = 1) { tokenIssuerConsumer.fetchToken(any(), target1) }
        coVerify(exactly = 1) { tokenIssuerConsumer.fetchToken(any(), target2) }

        result1 shouldBe result3
        result2 shouldBe result4
        result1 shouldNotBe result2
        result3 shouldNotBe result4
    }

    @Test
    fun `Should throw EntraIdTokenException if exchangeprocess fails`() {
        assertNonCachingFetcherThrows { IllegalArgumentException() }
        assertNonCachingFetcherThrows { SocketTimeoutException() }
        assertNonCachingFetcherThrows { Exception() }
        assertCachingFetcherThrows { IllegalArgumentException() }
        assertCachingFetcherThrows { SocketTimeoutException() }
        assertCachingFetcherThrows { Exception() }

    }

    fun assertNonCachingFetcherThrows(throwable: () -> Throwable) = run {
        NonCachingEntraIdTokenFetcher(
            tokenIssuerConsumer = mockk<TokenIssuerConsumer>().apply {
                coEvery { fetchToken(any(), any()) } throws throwable()
            },
            clientAssertionService
        ).apply {
            assertThrows<EntraIdTokenException> { runBlocking { getAccessToken("appappapp") } }
        }
    }

    fun assertCachingFetcherThrows(throwable: () -> Throwable) = run {
        CachingEntraIdTokenFetcher(
            tokenIssuerConsumer = mockk<TokenIssuerConsumer>().apply {
                coEvery { fetchToken(any(), any()) } throws throwable()
            },
            clientAssertionService,
            maxCacheEntries = 1,
            cacheExpiryMarginSeconds = 6

        ).apply {
            assertThrows<EntraIdTokenException> { runBlocking { getAccessToken("token") } }
        }
    }

    private fun tokenResponse(accessToken: String, expiresIn: Int = 300) = TokenResponse(
        accessToken = accessToken,
        expiresIn = expiresIn,
    )
}

