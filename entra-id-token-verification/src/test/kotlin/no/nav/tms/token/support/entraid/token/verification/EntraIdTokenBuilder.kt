package no.nav.tms.token.support.entraid.token.verification

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.Duration
import java.util.Date
import java.util.UUID

internal class EntraIdTokenBuilder(
    private val azureJwk: RSAKey,
    private val defaultIssuer: String,
    private val defaultAudience: String,
    private val defaultIssuedFor: NaisApplication,
) {

    fun azureSystemToken(
        client: NaisApplication,
        audience: String = defaultAudience,
        issuer: String = defaultIssuer,
        issueTime: Date = Date(),
        expiryTime: Date = Date() + Duration.ofHours(1)
    ): String {
        return JWTClaimsSet.Builder()
            .issuer(issuer)
            .issueTime(issueTime)
            .expirationTime(expiryTime)
            .audience(audience)
            .azpName(client)
            .jwtID(UUID.randomUUID().toString())
            .build()
            .sign(azureJwk)
            .serialize()
    }

    fun azureUserToken(
        navIdent: String,
        name: String = "Navn Navnesen",
        email: String = "navn.navnesen@nav.no",
        oid: String? = "<uuid>",
        audience: String = defaultAudience,
        issuer: String = defaultIssuer,
        issuedFor: NaisApplication = defaultIssuedFor,
        issueTime: Date = Date(),
        expiryTime: Date = Date() + Duration.ofHours(1)
    ): String {
        return JWTClaimsSet.Builder()
            .issuer(issuer)
            .issueTime(issueTime)
            .expirationTime(expiryTime)
            .audience(audience)
            .claim(EntraIdUserPrincipal.NAV_IDENT_CLAIM_NAME, navIdent)
            .claim(EntraIdUserPrincipal.DISPLAY_NAME_CLAIM_NAME, name)
            .claim(EntraIdUserPrincipal.USERNAME_CLAIM_NAME, email)
            .claim(EntraIdUserPrincipal.OID_CLAIM_NAME, oid)
            .azpName(issuedFor)
            .jwtID(UUID.randomUUID().toString())
            .build()
            .sign(azureJwk)
            .serialize()
    }

    private fun JWTClaimsSet.sign(rsaKey: RSAKey): SignedJWT =
        SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKey.keyID)
                .type(JOSEObjectType.JWT).build(),
            this
        ).apply {
            sign(RSASSASigner(rsaKey.toPrivateKey()))
        }

    private fun JWTClaimsSet.Builder.azpName(naisApp: NaisApplication): JWTClaimsSet.Builder {
        val claimValue = "${naisApp.cluster}:${naisApp.namespace}:${naisApp.app}"

        claim(NaisApplication.AZP_CLAIM_NAME, claimValue)

        return this
    }
}
