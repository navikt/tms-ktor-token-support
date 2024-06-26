package no.nav.tms.token.support.azure.exchange

import com.nimbusds.jwt.SignedJWT
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.azure.exchange.consumer.AzureConsumer
import no.nav.tms.token.support.azure.exchange.service.AzureExchangeException
import no.nav.tms.token.support.azure.exchange.service.CachingAzureService
import no.nav.tms.token.support.azure.exchange.service.NonCachingAzureService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.SocketTimeoutException

internal class AzureServiceTest {

    private val azureConsumer: AzureConsumer = mockk()
    private val jwtAudience = "https://azure.url/token"
    private val clientId = "cluster.namespace.thisApi"
    private val privateJwk = JwkBuilder.generateJwk()

    private val nonCachingazureService = NonCachingAzureService(azureConsumer, jwtAudience, clientId, privateJwk)
    private val cachingAzureService = CachingAzureService(azureConsumer, jwtAudience, clientId, privateJwk, 10, 5)

    @AfterEach
    fun cleanup() {
        clearMocks(azureConsumer)
    }

    @Test
    fun `Non-caching service should sign a jwt with correct claims and retrieve token from response`() {
        val assertion = slot<String>()
        val exchangedToken = "<exchanged token>"
        val target = "cluster.namespace.otherApi"

        coEvery {
            azureConsumer.fetchToken(capture(assertion), target)
        } returns AzureResponseObjectMother.createAzureResponse(exchangedToken)

        val result = runBlocking {
            nonCachingazureService.getAccessToken(target)
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
        val exchangedToken = "<exchanged token>"
        val target = "cluster.namespace.otherApi"

        coEvery {
            azureConsumer.fetchToken(capture(assertion), target)
        } returns AzureResponseObjectMother.createAzureResponse(exchangedToken)

        val result = runBlocking {
            cachingAzureService.getAccessToken(target)
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
        val exchangedToken = "<exchanged token>"
        val target = "cluster.namespace.otherApi"

        coEvery {
            azureConsumer.fetchToken(capture(assertion), target)
        } returns AzureResponseObjectMother.createAzureResponse(exchangedToken)

        runBlocking {
            cachingAzureService.getAccessToken(target)
            cachingAzureService.getAccessToken(target)
            cachingAzureService.getAccessToken(target)
        }

        coVerify(exactly = 1) { azureConsumer.fetchToken(any(), target) }
    }

    @Test
    fun `CachingService should make external calls when access token is missing or expired`() {
        val assertion = slot<String>()
        val exchangedToken = "<exchanged token>"
        val target = "cluster.namespace.otherApi"

        coEvery {
            azureConsumer.fetchToken(capture(assertion), target)
        } returns AzureResponseObjectMother.createAzureResponse(exchangedToken, expiresIn = 0)

        runBlocking {
            cachingAzureService.getAccessToken(target)
            cachingAzureService.getAccessToken(target)
            cachingAzureService.getAccessToken(target)
        }

        coVerify(exactly = 3) { azureConsumer.fetchToken(any(), target) }
    }

    @Test
    fun `CachingService should cache one unique token per target`() {
        val assertion = slot<String>()
        val exchangedToken1 = "<exchanged token 1>"
        val exchangedToken2 = "<exchanged token 2>"
        val target1 = "cluster.namespace.otherApi1"
        val target2 = "cluster.namespace.otherApi2"

        coEvery {
            azureConsumer.fetchToken(capture(assertion), target1)
        } returns AzureResponseObjectMother.createAzureResponse(exchangedToken1)

        coEvery {
            azureConsumer.fetchToken(capture(assertion), target2)
        } returns AzureResponseObjectMother.createAzureResponse(exchangedToken2)

        val result1 = runBlocking { cachingAzureService.getAccessToken(target1) }
        val result2 = runBlocking { cachingAzureService.getAccessToken(target2) }
        val result3 = runBlocking { cachingAzureService.getAccessToken(target1) }
        val result4 = runBlocking { cachingAzureService.getAccessToken(target2) }

        coVerify(exactly = 1) { azureConsumer.fetchToken(any(), target1) }
        coVerify(exactly = 1) { azureConsumer.fetchToken(any(), target2) }

        result1 shouldBe result3
        result2 shouldBe result4
        result1 shouldNotBe result2
        result3 shouldNotBe result4
    }

    @Test
    fun `Should throw AzureExchangeException if exchangeprocess fails`() {
        assertNonCachingServiceThrows { IllegalArgumentException() }
        assertNonCachingServiceThrows { SocketTimeoutException() }
        assertNonCachingServiceThrows { Error() }
        assertCachingServiceThrows { IllegalArgumentException() }
        assertCachingServiceThrows { SocketTimeoutException() }
        assertCachingServiceThrows { Error() }

    }

    fun assertNonCachingServiceThrows( throwable: () -> Throwable) = run {
        NonCachingAzureService(
            azureConsumer = mockk<AzureConsumer>().apply {
                coEvery { fetchToken(any(), any()) } throws throwable()
            },
            clientId = "some:client",
            issuer = "some:issuer",
            privateJwk = privateJwk,
        ).apply {
            assertThrows<AzureExchangeException> { runBlocking { getAccessToken("appappapp") } }
        }
    }

    fun assertCachingServiceThrows(throwable: () -> Throwable) = run {
        CachingAzureService(
            azureConsumer = mockk<AzureConsumer>().apply {
                coEvery { fetchToken(any(), any()) } throws throwable()
            },
            clientId = "some:client",
            privateJwk = privateJwk,
            issuer = "some:issuer",
            maxCacheEntries = 1,
            cacheExpiryMarginSeconds = 6

        ).apply {
            assertThrows<AzureExchangeException> { runBlocking { getAccessToken("token") } }
        }
    }


}

