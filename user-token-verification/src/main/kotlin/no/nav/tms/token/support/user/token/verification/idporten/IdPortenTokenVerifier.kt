package no.nav.tms.token.support.user.token.verification.idporten

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import no.nav.tms.token.support.user.token.verification.TokenVerifier
import no.nav.tms.token.support.user.token.verification.UserPrincipal
import java.security.interfaces.RSAPublicKey

internal class IdPortenTokenVerifier private constructor(
    val issuer: String,
    private val jwkProvider: JwkProvider,
    private val minLevelOfAssurance: IdPortenLevelOfAssurance
): TokenVerifier {

    private val acrClaim = "acr"

    companion object {
        fun build(
            jwkProvider: JwkProvider,
            issuer: String,
            minLevelOfAssurance: IdPortenLevelOfAssurance
        ) = IdPortenTokenVerifier(
            jwkProvider = jwkProvider,
            issuer = issuer,
            minLevelOfAssurance = minLevelOfAssurance,
        )
    }

    override fun verify(accessToken: DecodedJWT): UserPrincipal {
        val decodedJWT = buildVerifier(accessToken)
            .verify(accessToken)
            .also { verifyMinimumLoA(it) }

        return IdPortenUserPrincipal.fromJwt(decodedJWT)
    }

    private fun buildVerifier(accessToken: DecodedJWT): JWTVerifier {
        return accessToken.keyId
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


