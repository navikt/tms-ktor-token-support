package no.nav.tms.token.support.entraid.token.verification.install

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.annotation.JsonAlias
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.entraid.token.verification.EntraIdEnvironment
import java.net.URI
import java.util.concurrent.TimeUnit

internal object EntraIdVerifierBuilder {

    internal fun buildTokenVerifier(
        accessFilter: AccessFilter,
        enableDefaultProxy: Boolean,
    ) : TokenVerifier {

        val audience = getAzureEnvVar("AZURE_APP_CLIENT_ID")
        val wellKnownUrl = getAzureEnvVar("AZURE_APP_WELL_KNOWN_URL")

        val metadata = fetchMetadata(wellKnownUrl, enableDefaultProxy)

        val jwkProvider = JwkProviderBuilderWrapper.createJwkProvider(metadata.jwksUri)

        return TokenVerifier(
            jwkProvider = jwkProvider,
            issuer = metadata.issuer,
            audience = audience,
            accessFilter = accessFilter
        )
    }

    private fun fetchMetadata(wellKnownUrl: String, enableDefaultProxy: Boolean): OauthServerConfigurationMetadata = runBlocking {

        val client = HttpClientBuilder.build(enableDefaultProxy)

        client.request {
            method = HttpMethod.Get
            url(wellKnownUrl)
            accept(ContentType.Application.Json)
        }.body()
    }

    private data class OauthServerConfigurationMetadata(
        @param:JsonAlias("issuer") val issuer: String,
        @param:JsonAlias("jwks_uri") val jwksUri: String,
    )

    private fun getAzureEnvVar(varName: String) = EntraIdEnvironment.get(varName)
        ?: throw IllegalArgumentException("Fant ikke $varName for entraid/azure. Påse at nais.yaml er konfigurert riktig.")
}


internal object JwkProviderBuilderWrapper {
    fun createJwkProvider(jwksUri: String): JwkProvider =
        JwkProviderBuilder(URI.create(jwksUri).toURL())
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
}
