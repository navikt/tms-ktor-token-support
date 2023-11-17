package no.nav.tms.token.support.tokenx.validation.install

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URL
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.TimeUnit

private fun getTokenxClientId() = getTokenxEnvVar("TOKEN_X_CLIENT_ID")
private fun getTokenxWellKnownUrl() = getTokenxEnvVar("TOKEN_X_WELL_KNOWN_URL")

internal fun initializeTokenVerifier(minLevelOfAssurance: IdPortenLevelOfAssurance): TokenVerifier {
    val metadata = fetchMetadata(
        httpClient = HttpClientBuilder.build(),
        wellKnownUrl = getTokenxWellKnownUrl()
    )

    val jwkProvider = JwkProviderBuilder.createJwkProvider(metadata)

    return TokenVerifier(
        jwkProvider = jwkProvider,
        clientId = getTokenxClientId(),
        issuer = metadata.issuer,
        minLevelOfAssurance = minLevelOfAssurance
    )
}

internal class TokenVerifier(
    private val jwkProvider: JwkProvider,
    private val clientId: String,
    private val issuer: String,
    private val minLevelOfAssurance: IdPortenLevelOfAssurance
) {

    private val acrClaim = "acr"

    fun verify(accessToken: String): DecodedJWT {
        return buildVerifier(accessToken)
            .verify(accessToken)
            .also { verifyLevelOfAssurance(it) }
    }

    private fun buildVerifier(accessToken: String): JWTVerifier {
        return JWT.decode(accessToken).keyId
            .let { kid -> jwkProvider.get(kid) }
            .let { JWT.require(it.RSA256()) }
            .withIssuer(issuer)
            .withAudience(clientId)
            .build()
    }

    private fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)

    private fun verifyLevelOfAssurance(decodedToken: DecodedJWT) {
        val acrClaim = decodedToken.getClaim(acrClaim)

        val levelOfAssurance = IdPortenLevelOfAssurance.fromAcr(acrClaim.asString())

        if (levelOfAssurance.relativeValue < minLevelOfAssurance.relativeValue) {
            throw RuntimeException("Level of assurance too low")
        }
    }
}

@Serializable
internal data class OauthServerConfigurationMetadata(
    @SerialName("issuer") val issuer: String,
    @SerialName("token_endpoint") val tokenEndpoint: String,
    @SerialName("jwks_uri") val jwksUri: String,
    @SerialName("authorization_endpoint") var authorizationEndpoint: String = ""
)

private fun fetchMetadata(httpClient: HttpClient, wellKnownUrl: String): OauthServerConfigurationMetadata = runBlocking {
    httpClient.request {
        method = HttpMethod.Get
        url(wellKnownUrl)
        accept(ContentType.Application.Json)
    }.body()
}

internal object JwkProviderBuilder {
    fun createJwkProvider(metadata: OauthServerConfigurationMetadata): JwkProvider =
        com.auth0.jwk.JwkProviderBuilder(URL(metadata.jwksUri))
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
}

private fun getTokenxEnvVar(varName: String): String {
    return System.getenv(varName)
        ?: throw IllegalArgumentException("Fant ikke $varName for tokenx. Påse at nais.yaml er konfigurert riktig.")
}
