package no.nav.tms.token.support.entraid.token.verification.install

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import no.nav.tms.token.support.entraid.token.verification.EntraIdPrincipal
import no.nav.tms.token.support.entraid.token.verification.EntraIdUserPrincipal
import no.nav.tms.token.support.entraid.token.verification.NaisApplication
import java.security.interfaces.RSAPublicKey

internal class TokenVerifier(
    private val jwkProvider: JwkProvider,
    private val audience: String,
    private val issuer: String,
    private val accessFilter: AccessFilter
) {

    fun verify(accessToken: String): EntraIdPrincipal {
        val decodedJwt = JWT.decode(accessToken).keyId
            .let { kid -> jwkProvider.get(kid) }
            .run { azureAccessTokenVerifier(audience, issuer) }
            .run { verify(accessToken) }

        if (EntraIdUserPrincipal.isUserPrincipal(decodedJwt)) {
            if (!accessFilter.allowUserAccess) {
                throw EntraIdAccessException(
                    "Endepunkt er kun åpent for system-token",
                    "Token ble utstedt på vegne av bruker, men endepunktet krever system-token"
                )
            }

            return EntraIdUserPrincipal(decodedJwt)
        } else {
            validateSystemAccess(decodedJwt)

            return EntraIdPrincipal(decodedJwt)
        }
    }

    private fun Jwk.azureAccessTokenVerifier(clientId: String, issuer: String) =
        JWT.require(this.RSA256())
            .withAudience(clientId)
            .withIssuer(issuer)
            .build()

    private fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)

    private fun validateSystemAccess(decodedJWT: DecodedJWT) {
        if (accessFilter.allowAllSystems()) {
            return
        }

        val clientApplication = NaisApplication.fromClaims(decodedJWT)!!

        if (accessFilter.cluster != null && clientApplication.cluster != accessFilter.cluster) {
            throw EntraIdAccessException(
                "Godtar ikke token fordi det er utstedt til en applikasjon som ikke ligger i påkrevd cluster",
                "Token ble utstedt til $clientApplication, men påkrevd cluster er ${accessFilter.cluster}"
            )
        }

        if (accessFilter.namespace != null && clientApplication.namespace != accessFilter.namespace) {
            throw EntraIdAccessException(
                "Godtar ikke token fordi det er utstedt til en applikasjon som ikke ligger i påkrevd namespace",
                "Token ble utstedt til $clientApplication, men påkrevd cluster er ${accessFilter.namespace}"
            )
        }

        if (accessFilter.app != null && clientApplication.app != accessFilter.app) {
            throw EntraIdAccessException(
                "Godtar ikke token fordi det er utstedt til annen enn påkred applikasjon",
                "Token ble utstedt til $clientApplication, men påkrevd applikasjon er ${accessFilter.app}"
            )
        }
    }

    internal class EntraIdAccessException(
        msg: String,
        val description: String
    ): IllegalArgumentException(msg)
}

internal data class AccessFilter(
    val allowUserAccess: Boolean,
    val cluster: String?,
    val namespace: String?,
    val app: String?
) {
    fun allowAllSystems() = cluster == null
        && namespace == null
        && app == null
}
