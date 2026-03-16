package no.nav.tms.token.support.user.token.verification.tokenx

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.annotation.JsonAlias
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.user.token.verification.HttpClientBuilder
import no.nav.tms.token.support.user.token.verification.LevelOfAssurance
import no.nav.tms.token.support.user.token.verification.UserTokenVerificationEnvironment
import java.net.URI
import java.util.concurrent.TimeUnit

internal class TokenxVerifierBuilder(
    private val tokenxWellKnownUrl: String,
    enableProxy: Boolean
) {
    private val httpClient = HttpClientBuilder.buildHttpClient(enableProxy)

    private val tokenxClientId = getTokenxEnvVar("TOKEN_X_CLIENT_ID")

    fun buildTokenVerifier(minLevelOfAssurance: LevelOfAssurance): TokenxVerifier {
        val metadata = fetchMetadata()

        val jwkProvider = createJwkProvider(metadata)

        return TokenxVerifier(
            jwkProvider = jwkProvider,
            clientId = tokenxClientId,
            issuer = metadata.issuer,
            minLevelOfAssurance = TokenxLevelOfAssurance.fromLevelOfAssurance(minLevelOfAssurance)
        )
    }

    private fun getTokenxEnvVar(varName: String) = UserTokenVerificationEnvironment.get(varName)
        ?: throw IllegalArgumentException("Fant ikke $varName for tokenx. Påse at nais.yaml er konfigurert riktig.")

    private fun fetchMetadata(): OauthServerConfigurationMetadata = runBlocking {
        httpClient.request {
            method = HttpMethod.Get
            url(tokenxWellKnownUrl)
            accept(ContentType.Application.Json)
        }.body()
    }

    private fun createJwkProvider(metadata: OauthServerConfigurationMetadata): JwkProvider =
        JwkProviderBuilder(URI.create(metadata.jwksUri).toURL())
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

    private data class OauthServerConfigurationMetadata(
        @param:JsonAlias("issuer") val issuer: String,
        @param:JsonAlias("token_endpoint") val tokenEndpoint: String,
        @param:JsonAlias("jwks_uri") val jwksUri: String,
        @param:JsonAlias("authorization_endpoint") var authorizationEndpoint: String = ""
    )
}
