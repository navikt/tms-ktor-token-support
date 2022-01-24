package no.nav.tms.token.support.idporten.wonderwall.authentication

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import java.security.interfaces.RSAPublicKey

internal class TokenVerifier(
        private val jwkProvider: JwkProvider,
        private val clientId: String,
        private val issuer: String,
        private val minLoginLevel: Int
) {

    private val claimContainingLoginLevel = "acr"

    fun verifyAccessToken(accessToken: String): DecodedJWT {
        val decodedToken = JWT.decode(accessToken).keyId
            .let { kid -> jwkProvider.get(kid) }
            .run { accessTokenVerifier(clientId, issuer) }
            .run { verify(accessToken) }

        verifyLoginLevel(decodedToken)

        return decodedToken
    }

    private fun Jwk.accessTokenVerifier(clientId: String, issuer: String): JWTVerifier =
            JWT.require(this.RSA256())
                .withClaim("client_id", clientId)
                .withIssuer(issuer)
                .build()

    private fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)

    private fun verifyLoginLevel(decodedToken: DecodedJWT) {
        val acrClaim = decodedToken.getClaim(claimContainingLoginLevel)

        val loginLevel = extractNumericValue(acrClaim.asString())

        if (loginLevel < minLoginLevel) {
            throw RuntimeException("Login level too low")
        }
    }

    private val acrRegex = "^Level([0-9]+)$".toRegex()

    private fun extractNumericValue(loginLevelClaim: String): Int {
        val loginLevel = acrRegex.find(loginLevelClaim)
            ?.destructured
            ?.let { (level) -> level.toInt() }

        return loginLevel ?: throw RuntimeException("Could not extract login level from claim")
    }
}


