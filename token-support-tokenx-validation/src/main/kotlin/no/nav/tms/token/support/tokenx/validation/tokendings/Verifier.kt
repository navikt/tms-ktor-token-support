package no.nav.tms.token.support.tokenx.validation.tokendings

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import java.security.interfaces.RSAPublicKey

internal object Verifier {
    fun createVerifier(jwkProvider: JwkProvider, clientId: String, issuer: String): (String) -> JWTVerifier? = {
        jwkProvider.get(JWT.decode(it).keyId).idTokenVerifier(
                clientId,
                issuer
        )
    }

    private fun Jwk.idTokenVerifier(clientId: String, issuer: String): JWTVerifier =
            JWT.require(this.RSA256())
                    .withAudience(clientId)
                    .withIssuer(issuer)
                    .build()

    private fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)
}


