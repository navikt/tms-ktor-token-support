package no.nav.tms.token.support.azure.validation.install

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.fasterxml.jackson.annotation.JsonAlias
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.azure.validation.AzureEnvironment
import java.net.URL
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.TimeUnit

private fun getAzureClientId() = getAzureEnvVar("AZURE_APP_CLIENT_ID")
private fun getAzureWellKnownUrl() = getAzureEnvVar("AZURE_APP_WELL_KNOWN_URL")

internal fun initializeTokenVerifier(
    enableDefaultProxy: Boolean,
) : TokenVerifier {

    val metadata = fetchMetadata(
        client = HttpClientBuilder.build(enableDefaultProxy),
        wellKnownUrl = getAzureWellKnownUrl()
    )

    val jwkProvider = JwlProviderBuilder.createJwkProvider(metadata)

    return TokenVerifier(
        jwkProvider = jwkProvider,
        issuer = metadata.issuer,
        clientId = getAzureClientId()
    )
}

internal class TokenVerifier(
    private val jwkProvider: JwkProvider,
    private val clientId: String,
    private val issuer: String
) {

    fun verify(accessToken: String): DecodedJWT {
        return JWT.decode(accessToken).keyId
            .let { kid -> jwkProvider.get(kid) }
            .run { azureAccessTokenVerifier(clientId, issuer) }
            .run { verify(accessToken) }
    }

    private fun Jwk.azureAccessTokenVerifier(clientId: String, issuer: String): JWTVerifier =
        JWT.require(this.RSA256())
            .withAudience(clientId)
            .withIssuer(issuer)
            .build()

    private fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)
}


internal data class OauthServerConfigurationMetadata(
    @JsonAlias("issuer") val issuer: String,
    @JsonAlias("token_endpoint") val tokenEndpoint: String,
    @JsonAlias("jwks_uri") val jwksUri: String,
    @JsonAlias("authorization_endpoint") var authorizationEndpoint: String = ""
)

private fun fetchMetadata(client: HttpClient, wellKnownUrl: String): OauthServerConfigurationMetadata = runBlocking {
    client.request {
        method = HttpMethod.Get
        url(wellKnownUrl)
        accept(ContentType.Application.Json)
    }.body()
}

internal object JwlProviderBuilder {
    fun createJwkProvider(metadata: OauthServerConfigurationMetadata): JwkProvider =
        com.auth0.jwk.JwkProviderBuilder(URL(metadata.jwksUri))
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
}

private fun getAzureEnvVar(varName: String) = AzureEnvironment.get(varName)
    ?: throw IllegalArgumentException("Fant ikke $varName for azure. Påse at nais.yaml er konfigurert riktig.")
