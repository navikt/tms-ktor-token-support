package no.nav.tms.token.support.idporten.sidecar.install

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
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

private fun getIdportenWellKnownUrl() = System.getenv("IDPORTEN_WELL_KNOWN_URL")
    ?: throw IllegalArgumentException("Fant ikke IDPORTEN_WELL_KNOWN_URL som brukes i token-support-idporten-sidecar. Påse at nais.yaml er konfigurert riktig.")

internal fun initializeTokenVerifier(
    enableDefaultProxy: Boolean,
    minLevelOfAssurance: IdPortenLevelOfAssurance?,
) : TokenVerifier {

    val metadata = fetchMetadata(
        client = HttpClientBuilder.buildHttpClient(enableDefaultProxy),
        wellKnownUrl = getIdportenWellKnownUrl()
    )

    val jwkProvider = createJwkProvider(metadata)

    return TokenVerifier.build(
        jwkProvider = jwkProvider,
        issuer = metadata.issuer,
        minLevelOfAssurance = minLevelOfAssurance
    )
}

internal class TokenVerifier private constructor(
    private val jwkProvider: JwkProvider,
    private val issuer: String,
    private val minLevelOfAssurance: IdPortenLevelOfAssurance?
) {

    private val acrClaim = "acr"

    companion object {
        fun build(
            jwkProvider: JwkProvider,
            issuer: String,
            minLevelOfAssurance: IdPortenLevelOfAssurance?
        ) = TokenVerifier(
            jwkProvider = jwkProvider,
            issuer = issuer,
            minLevelOfAssurance = minLevelOfAssurance,
        )
    }

    fun verifyAccessToken(accessToken: String): DecodedJWT {
        return buildVerifier(accessToken)
            .verify(accessToken)
            .also { verifyMinimumLoA(it) }
    }

    private fun buildVerifier(accessToken: String): JWTVerifier {
        return JWT.decode(accessToken).keyId
            .let { kid -> jwkProvider.get(kid) }
            .let { JWT.require(it.RSA256()) }
            .withIssuer(issuer)
            .build()
    }

    private fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)

    private fun verifyMinimumLoA(decodedToken: DecodedJWT) {
        if (minLevelOfAssurance == null) {
            return
        }

        val acrClaim = decodedToken.getClaim(acrClaim)

        val levelOfAssurance = IdPortenLevelOfAssurance.fromAcr(acrClaim.asString())

        if (levelOfAssurance.relativeValue < minLevelOfAssurance.relativeValue) {
            throw RuntimeException("Level of assurance too low.")
        }
    }
}

@Serializable
internal data class OauthServerConfigurationMetadata(
    @SerialName("issuer") val issuer: String,
    @SerialName("jwks_uri") val jwksUri: String,
)

private fun fetchMetadata(client: HttpClient, wellKnownUrl: String): OauthServerConfigurationMetadata = runBlocking {
    client.request {
        method = HttpMethod.Get
        url(wellKnownUrl)
        accept(ContentType.Application.Json)
    }.body()
}

private fun createJwkProvider(metadata: OauthServerConfigurationMetadata): JwkProvider = JwkProviderBuilder(URL(metadata.jwksUri))
    .cached(10, 24, TimeUnit.HOURS)
    .rateLimited(10, 1, TimeUnit.MINUTES)
    .build()


