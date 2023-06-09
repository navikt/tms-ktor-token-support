package no.nav.tms.token.support.idporten.sidecar.authentication

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import java.security.interfaces.RSAPublicKey

internal class TokenVerifier(
        private val jwkProvider: JwkProvider,
        private val issuer: String,
        private val minLevelOfAssurance: LevelOfAssuranceInternal
) {

    private val acrClaim = "acr"

    fun verifyAccessToken(accessToken: String): DecodedJWT {
        return buildVerifier(accessToken)
            .verify(accessToken)
            .also { verifyLevelOfAssurance(it) }
    }

    private fun buildVerifier(accessToken: String): JWTVerifier {
        return JWT.decode(accessToken).keyId
            .let { kid -> jwkProvider.get(kid) }
            .let { JWT.require(it.RSA256()) }
            .withIssuer(issuer)
            .build()
    }

    private fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)

    private fun verifyLevelOfAssurance(decodedToken: DecodedJWT) {
        val acrClaim = decodedToken.getClaim(acrClaim)

        val levelOfAssurance = LevelOfAssuranceInternal.fromAcr(acrClaim.asString())

        if (levelOfAssurance.relativeValue < minLevelOfAssurance.relativeValue) {
            throw RuntimeException("Level of assurance too low.")
        }
    }
}


