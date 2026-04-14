package no.nav.tms.token.support.entraid.token.fetcher

import no.nav.tms.token.support.entraid.token.fetcher.impl.TokenIssuerConsumer
import no.nav.tms.token.support.entraid.token.fetcher.impl.CachingEntraIdTokenFetcher
import no.nav.tms.token.support.entraid.token.fetcher.impl.ClientAssertionService
import no.nav.tms.token.support.entraid.token.fetcher.impl.HttpClientBuilder
import no.nav.tms.token.support.entraid.token.fetcher.impl.NonCachingEntraIdTokenFetcher

object EntraIdTokenFetcherBuilder {

    private val clientId: String = getAzureEnvVar("AZURE_APP_CLIENT_ID")
    private val tenantId: String = getAzureEnvVar("AZURE_APP_TENANT_ID")
    private val privateJwk: String = getAzureEnvVar("AZURE_APP_JWK")
    private val openidIssuer: String = getAzureEnvVar("AZURE_OPENID_CONFIG_ISSUER")
    private val openidTokenEndpoint: String = getAzureEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")

    fun buildFetcher(
        cachingEnabled: Boolean = true,
        maxCachedEntries: Long = 1000L,
        cacheExpiryMarginSeconds: Int = 5,
        enableDefaultProxy: Boolean = false
    ): EntraIdTokenFetcher {

        val httpClient = HttpClientBuilder.buildHttpClient(enableDefaultProxy)

        val tokenIssuerConsumer = TokenIssuerConsumer(
            httpClient = httpClient,
            tentantId = tenantId,
            clientId = clientId,
            openidTokenEndpoint
        )

        val clientAssertionService = ClientAssertionService(
            audience = openidIssuer,
            clientId = clientId,
            privateJwk = privateJwk
        )

        if (cachingEnabled) {
            require(maxCachedEntries > 0) { "'maxCachedEntries' should be at least 1" }
            require(cacheExpiryMarginSeconds > 0) { "'cacheExpiryMarginSeconds' should be at least 1" }
        }

        return if (cachingEnabled) {
            CachingEntraIdTokenFetcher(
                tokenIssuerConsumer = tokenIssuerConsumer,
                clientAssertionService = clientAssertionService,
                maxCacheEntries = maxCachedEntries,
                cacheExpiryMarginSeconds = cacheExpiryMarginSeconds
            )
        } else {
            NonCachingEntraIdTokenFetcher(
                tokenIssuerConsumer = tokenIssuerConsumer,
                clientAssertionService = clientAssertionService
            )
        }
    }

    private fun getAzureEnvVar(varName: String) = EntraIdEnvironment.get(varName)
        ?: throw IllegalArgumentException("Fant ikke $varName for entra-id/azure. Påse at nais.yaml er konfigurert riktig.")

}
