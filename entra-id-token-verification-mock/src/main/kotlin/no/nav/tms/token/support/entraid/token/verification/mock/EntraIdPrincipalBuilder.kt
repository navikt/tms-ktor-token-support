package no.nav.tms.token.support.entraid.token.verification.mock

import com.auth0.jwt.JWT
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.tms.token.support.entraid.token.verification.EntraIdPrincipal
import no.nav.tms.token.support.entraid.token.verification.EntraIdUserPrincipal
import no.nav.tms.token.support.entraid.token.verification.NaisApplication
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.util.*

internal object EntraIdPrincipalBuilder {

    const val ENTRA_ID_ISSUER = "https://azure"

    private val privateJwk = JwkBuilder.generateJwk()

    fun createPrincipal(authentication: Authentication): EntraIdPrincipal {
        val decodedJWT = JWTClaimsSet.Builder()
            .issueTime(Date.from(Instant.now()))
            .expirationTime(Date.from(Instant.now().plus(1, HOURS)))
            .jwtID("STUB")
            .claim("iss", ENTRA_ID_ISSUER)
            .azpName(authentication.issuedFor)
            .userContext(authentication.userInfo)
            .build()
            .sign()
            .serialize()
            .let(JWT::decode)


        return EntraIdPrincipal(decodedJWT)
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

    private fun JWTClaimsSet.Builder.azpName(naisApp: NaisApplication): JWTClaimsSet.Builder {
        val claimValue = "${naisApp.cluster}:${naisApp.namespace}:${naisApp.app}"

        claim(NaisApplication.AZP_CLAIM_NAME, claimValue)

        return this
    }

    private fun JWTClaimsSet.Builder.userContext(userInfo: UserInfo?): JWTClaimsSet.Builder {

        if (userInfo != null) {
            claim(EntraIdUserPrincipal.NAV_IDENT_CLAIM_NAME, userInfo.navIdent)
            claim(EntraIdUserPrincipal.OID_CLAIM_NAME, userInfo.userId)
            claim(EntraIdUserPrincipal.DISPLAY_NAME_CLAIM_NAME, userInfo.displayName)
            claim(EntraIdUserPrincipal.USERNAME_CLAIM_NAME, userInfo.userName)
        }

        return this
    }
}
