package no.nav.tms.token.support.user.token.verification

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.fasterxml.jackson.annotation.JsonAlias
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.TimeUnit

internal class TokenVerifier(
    val issuer: String,
    private val jwkProvider: JwkProvider,
    private val audience: String,
    private val acrMapper: (String) -> LevelOfAssurance,
    private val minLevelOfAssurance: LevelOfAssurance
) {
    private val identClaim = "pid"
    private val acrClaim = "acr"

    fun verify(accessToken: DecodedJWT): UserPrincipal {
        val decodedJWT = buildVerifier(accessToken)
            .verify(accessToken)
            .also { verifyMinimumLoA(it) }

        return principalFromJwt(decodedJWT)
    }

    private fun buildVerifier(accessToken: DecodedJWT): JWTVerifier {
        return accessToken.keyId
            .let { kid -> jwkProvider.get(kid) }
            .let { JWT.require(it.RSA256()) }
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
    }

    private fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)

    private fun verifyMinimumLoA(decodedToken: DecodedJWT) {

        val acrClaim = decodedToken.getClaim(acrClaim)

        val levelOfAssurance = acrMapper(acrClaim.asString())

        if (levelOfAssurance < minLevelOfAssurance) {
            throw RuntimeException("Level of assurance too low.")
        }
    }

    private fun principalFromJwt(jwt: DecodedJWT): UserPrincipal {
        return UserPrincipal(
            accessToken = jwt.token,
            ident = jwt.getClaim(identClaim).asString(),
            levelOfAssurance = acrMapper(jwt.getClaim(acrClaim).asString())
        )
    }

    companion object {
        fun build(
            wellKnownUrl: String,
            audience: String,
            acrMapper: (String) -> LevelOfAssurance,
            minLevelOfAssurance: LevelOfAssurance,
            webProxy: Boolean
        ): TokenVerifier {
            val metadata = fetchMetadata(wellKnownUrl, webProxy)

            val jwkProvider = JwkProviderBuilderWrapper.createJwkProvider(metadata.jwksUri)

            return TokenVerifier(
                issuer = metadata.issuer,
                jwkProvider = jwkProvider,
                audience = audience,
                minLevelOfAssurance = minLevelOfAssurance,
                acrMapper = acrMapper
            )
        }

        private fun fetchMetadata(wellKnownUrl: String, webProxy: Boolean): OauthServerConfigurationMetadata = runBlocking {
            val httpClient = HttpClientBuilder.buildHttpClient(webProxy)

            httpClient.request {
                method = HttpMethod.Get
                url(wellKnownUrl)
                accept(ContentType.Application.Json)
            }.body()
        }

        data class OauthServerConfigurationMetadata(
            @param:JsonAlias("issuer") val issuer: String,
            @param:JsonAlias("jwks_uri") val jwksUri: String,
        )
    }
}

internal object JwkProviderBuilderWrapper {
    fun createJwkProvider(jwksUri: String): JwkProvider =
        JwkProviderBuilder(URI.create(jwksUri).toURL())
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
}
