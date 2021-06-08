package no.nav.tms.token.support.idporten.authentication

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
        private val issuer: String
) {

    fun verifyIdToken(accessToken: String): DecodedJWT {
        return JWT.decode(accessToken).keyId
                .let { kid -> jwkProvider.get(kid) }
                .run { idTokenVerifier(clientId, issuer) }
                .run { verify(accessToken) }
    }

    fun verifyAccessToken(accessToken: String): DecodedJWT {
        return JWT.decode(accessToken).keyId
                .let { kid -> jwkProvider.get(kid) }
                .run { accessTokenVerifier(clientId, issuer) }
                .run { verify(accessToken) }
    }

    private fun Jwk.idTokenVerifier(clientId: String, issuer: String): JWTVerifier =
            JWT.require(this.RSA256())
                    .withAudience(clientId)
                    .withIssuer(issuer)
                    .build()

    private fun Jwk.accessTokenVerifier(clientId: String, issuer: String): JWTVerifier =
            JWT.require(this.RSA256())
                    .withClaim("client_id", clientId)
                    .withIssuer(issuer)
                    .build()

    private fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)
}


