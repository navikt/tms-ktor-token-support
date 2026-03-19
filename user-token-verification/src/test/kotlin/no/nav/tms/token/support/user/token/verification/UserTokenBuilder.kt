package no.nav.tms.token.support.user.token.verification

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.id.Audience
import java.util.*

internal class UserTokenBuilder(
    private val idPortenUrl: String,
    private val idPortenJwk: RSAKey,
    private val idPortenAud: String,
    private val tokenxUrl: String,
    private val tokenxJwk: RSAKey
) {

    fun idportenToken(
        ident: String,
        audience: String = idPortenAud,
        acrClaim: String = IdPortenLoa.High.acr,
        issueTime: Date = Date(),
        expiryTime: Date = Date(Date().time + 3600000)
    ): String {
        return JWTClaimsSet.Builder()
            .issuer(idPortenUrl)
            .issueTime(issueTime)
            .expirationTime(expiryTime)
            .audience(audience)
            .claim("pid", ident)
            .claim("acr", acrClaim)
            .jwtID(UUID.randomUUID().toString())
            .build()
            .sign(idPortenJwk)
            .serialize()
    }

    fun tokenxToken(
        ident: String,
        target: String,
        acrClaim: String = TokenxLoa.High.acr,
        issueTime: Date = Date(),
        expiryTime: Date = Date(Date().time + 3600000)
    ): String {
        return JWTClaimsSet.Builder()
            .issuer(tokenxUrl)
            .issueTime(issueTime)
            .expirationTime(expiryTime)
            .audience(target)
            .claim("pid", ident)
            .claim("acr", acrClaim)
            .jwtID(UUID.randomUUID().toString())
            .build()
            .sign(tokenxJwk)
            .serialize()
    }

    enum class IdPortenLoa(val acr: String) {
        Low("idporten-loa-low"),
        Substantial("idporten-loa-substantial"),
        High("idporten-loa-high")
    }

    enum class TokenxLoa(val acr: String) {
        Substantial("Level3"),
        High("Level4")
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
}
