package no.nav.tms.token.support.user.token.exchange

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.user.token.exchange.impl.CachingExchanger
import no.nav.tms.token.support.user.token.exchange.impl.NonCachingExchanger
import no.nav.tms.token.support.user.token.exchange.impl.TokenExchangeConsumer

object UserTokenExchangerBuilder {

    private val httpClient = buildHttpClient()

    private val tokenxWellKnownUrl: String = getTokenxEnvVar("TOKEN_X_WELL_KNOWN_URL")
    private val tokenxClientId: String = getTokenxEnvVar("TOKEN_X_CLIENT_ID")
    private val tokenxClientJwk: String = getTokenxEnvVar("TOKEN_X_PRIVATE_JWK")

    private val metadata = fetchMetadata(tokenxWellKnownUrl)

    private val tokenExchangeConsumer = TokenExchangeConsumer(httpClient, metadata.tokenEndpoint)

    fun build(
        cachingEnabled: Boolean = true,
        maxCachedEntries: Long = 1000L,
        cacheExpiryMarginSeconds: Int = 5
    ): UserTokenExchanger {

        if (cachingEnabled) {
            require(maxCachedEntries > 0) { "'maxCachedEntries' should be at least 1" }
            require(cacheExpiryMarginSeconds > 0) { "'cacheExpiryMarginSeconds' should be at least 1" }
        }

        return if (cachingEnabled) {
            createCachingExchanger(maxCachedEntries, cacheExpiryMarginSeconds)
        } else {
            createNonCachingExchanger()
        }
    }

    private fun createNonCachingExchanger() = NonCachingExchanger(
        tokenExchangeConsumer = tokenExchangeConsumer,
        jwtAudience = metadata.tokenEndpoint,
        clientId = tokenxClientId,
        privateJwk = tokenxClientJwk
    )

    private fun createCachingExchanger(maxCachedEntries: Long, cacheExpiryMarginSeconds: Int) = CachingExchanger(
        tokenExchangeConsumer = tokenExchangeConsumer,
        jwtAudience = metadata.tokenEndpoint,
        clientId = tokenxClientId,
        privateJwk = tokenxClientJwk,
        maxCacheEntries = maxCachedEntries,
        cacheExpiryMarginSeconds = cacheExpiryMarginSeconds
    )

    private fun getTokenxEnvVar(varName: String) = UserTokenExchangeEnvironment.get(varName)
        ?: throw IllegalArgumentException("Fant ikke $varName for tokenx. Påse at nais.yaml er konfigurert riktig.")

    private fun buildHttpClient(): HttpClient {
        return HttpClient(Apache5) {
            install(ContentNegotiation) {
                jackson {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            install(HttpTimeout)
        }
    }

    private fun fetchMetadata(wellKnownUrl: String): TokenExchangeMetadata {
        return runBlocking {
            httpClient.request {
                method = HttpMethod.Get
                url(wellKnownUrl)
                accept(ContentType.Application.Json)
            }.body()
        }
    }

    data class TokenExchangeMetadata(
        @param:JsonAlias("issuer") val issuer: String,
        @param:JsonAlias("token_endpoint") val tokenEndpoint: String,
        @param:JsonAlias("jwks_uri") val jwksUri: String
    )
}
