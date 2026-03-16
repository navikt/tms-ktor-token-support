package no.nav.tms.token.support.user.token.verification.tokenx

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import no.nav.tms.token.support.user.token.verification.TokenVerifier
import no.nav.tms.token.support.user.token.verification.UserPrincipal
import java.security.interfaces.RSAPublicKey

internal class TokenxVerifier(
    val issuer: String,
    private val jwkProvider: JwkProvider,
    private val clientId: String,
    private val minLevelOfAssurance: TokenxLevelOfAssurance
): TokenVerifier {

    private val acrClaim = "acr"

    override fun verify(accessToken: DecodedJWT): UserPrincipal {
        val verifiedJwt =  buildVerifier(accessToken)
            .verify(accessToken)
            .also { verifyLevelOfAssurance(it) }

        return TokenxUserPrincipal.fromJwt(verifiedJwt)
    }

    private fun buildVerifier(accessToken: DecodedJWT): JWTVerifier {
        return accessToken.keyId
            .let { kid -> jwkProvider.get(kid) }
            .let { JWT.require(it.RSA256()) }
            .withIssuer(issuer)
            .withAudience(clientId)
            .build()
    }

    private fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)

    private fun verifyLevelOfAssurance(decodedToken: DecodedJWT) {
        val acrClaim = decodedToken.getClaim(acrClaim)

        val levelOfAssurance = TokenxLevelOfAssurance.fromAcr(acrClaim.asString())

        if (levelOfAssurance.relativeValue < minLevelOfAssurance.relativeValue) {
            throw RuntimeException("Level of assurance too low")
        }
    }
}
