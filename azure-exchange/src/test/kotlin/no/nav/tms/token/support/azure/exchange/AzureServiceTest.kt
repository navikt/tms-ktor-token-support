package no.nav.tms.token.support.azure.exchange

import com.nimbusds.jwt.SignedJWT
import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.azure.exchange.consumer.AzureConsumer
import no.nav.tms.token.support.azure.exchange.service.CachingAzureService
import no.nav.tms.token.support.azure.exchange.service.NonCachingAzureService
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

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
        val exchangedToken = "<exchanged token>"
        val target = "cluster.namespace.otherApi"

        coEvery {
            azureConsumer.fetchToken(capture(assertion), target)
        } returns AzureResponseObjectMother.createAzureResponse(exchangedToken)

        val result = runBlocking {
            cachingAzureService.getAccessToken(target)
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

        coVerify(exactly = 1) {azureConsumer.fetchToken(any(), target) }
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

        coVerify(exactly = 3) {azureConsumer.fetchToken(any(), target) }
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

        coVerify(exactly = 1) {azureConsumer.fetchToken(any(), target1) }
        coVerify(exactly = 1) {azureConsumer.fetchToken(any(), target2) }

        result1 `should be equal to` result3
        result2 `should be equal to` result4
        result1 `should not be equal to` result2
        result3 `should not be equal to` result4
    }
}
