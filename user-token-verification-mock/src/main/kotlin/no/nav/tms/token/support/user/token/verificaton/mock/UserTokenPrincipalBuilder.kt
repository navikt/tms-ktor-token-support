package no.nav.tms.token.support.user.token.verificaton.mock

import com.auth0.jwt.JWT
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.tms.token.support.user.token.verification.Issuer
import no.nav.tms.token.support.user.token.verification.LevelOfAssurance
import no.nav.tms.token.support.user.token.verification.UserPrincipal
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.util.*

internal object UserTokenPrincipalBuilder {

    private val privateJwk = JwkBuilder.generateJwk()

    fun createPrincipal(authentication: Authentication): UserPrincipal {
        val decodedJWT = JWTClaimsSet.Builder()
            .issueTime(Date.from(Instant.now()))
            .expirationTime(Date.from(Instant.now().plus(1, HOURS)))
            .jwtID("STUB")
            .claim("acr", mapLoa(authentication.levelOfAssurance))
            .claim("pid", authentication.ident)
            .claim("iss", mapIssuer(authentication.issuer))
            .build()
            .sign()
            .serialize()
            .let(JWT::decode)

        return UserPrincipal(authentication.ident, authentication.levelOfAssurance, decodedJWT)
    }

    private fun JWTClaimsSet.sign(): SignedJWT =
        SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(privateJwk.keyID)
                .type(JOSEObjectType.JWT).build(),
            this
        ).apply {
            sign(RSASSASigner(privateJwk.toPrivateKey()))
        }

    private fun mapLoa(levelOfAssurance: LevelOfAssurance) =
        when (levelOfAssurance) {
            LevelOfAssurance.High -> MockLoaHigh
            LevelOfAssurance.Substantial -> MockLoaSubstantial
        }

    private fun mapIssuer(issuer: Issuer) =
        when (issuer) {
            Issuer.IdPorten -> IdPortenMockIssuer
            Issuer.Tokenx -> TokenxMockIssuer
        }
}
