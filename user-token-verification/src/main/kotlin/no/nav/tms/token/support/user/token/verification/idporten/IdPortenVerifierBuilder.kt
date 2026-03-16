package no.nav.tms.token.support.user.token.verification.idporten

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.annotation.JsonAlias
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.user.token.verification.HttpClientBuilder
import no.nav.tms.token.support.user.token.verification.LevelOfAssurance
import no.nav.tms.token.support.user.token.verification.UserTokenVerificationEnvironment
import java.net.URI
import java.util.concurrent.TimeUnit

internal class IdPortenVerifierBuilder(
    private val idPortenWellKnownUrl: String,
    enableProxy: Boolean
) {
    private val httpClient = HttpClientBuilder.buildHttpClient(enableProxy)

    internal fun buildTokenVerifier(
        minLevelOfAssurance: LevelOfAssurance,
    ) : IdPortenTokenVerifier {

        val metadata = fetchMetadata()

        val jwkProvider = createJwkProvider(metadata)

        return IdPortenTokenVerifier.build(
            jwkProvider = jwkProvider,
            issuer = metadata.issuer,
            minLevelOfAssurance = IdPortenLevelOfAssurance.fromLevelOfAssurance(minLevelOfAssurance)
        )
    }

    private data class OauthServerConfigurationMetadata(
        @param:JsonAlias("issuer") val issuer: String,
        @param:JsonAlias("jwks_uri") val jwksUri: String,
    )

    private fun fetchMetadata(): OauthServerConfigurationMetadata = runBlocking {
        httpClient.request {
            method = HttpMethod.Get
            url(idPortenWellKnownUrl)
            accept(ContentType.Application.Json)
        }.body()
    }

    private fun createJwkProvider(metadata: OauthServerConfigurationMetadata): JwkProvider =
        URI.create(metadata.jwksUri).toURL()
            .let { JwkProviderBuilder(it) }
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

    private fun getIdportenWellKnownUrl() = UserTokenVerificationEnvironment.get("IDPORTEN_WELL_KNOWN_URL")
        ?: throw IllegalArgumentException("Fant ikke IDPORTEN_WELL_KNOWN_URL som brukes i token-support-idporten-sidecar. Påse at nais.yaml er konfigurert riktig.")
}
