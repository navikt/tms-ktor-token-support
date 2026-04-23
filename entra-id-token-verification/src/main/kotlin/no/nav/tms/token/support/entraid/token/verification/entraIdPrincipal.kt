package no.nav.tms.token.support.entraid.token.verification

import com.auth0.jwt.interfaces.DecodedJWT

open class EntraIdPrincipal(val decodedJWT: DecodedJWT) {
    val issuedFor: NaisApplication? = NaisApplication.fromClaims(decodedJWT)
}

class EntraIdUserPrincipal(decodedJWT: DecodedJWT): EntraIdPrincipal(decodedJWT) {
    val navIdent: String = decodedJWT.getClaim(NAV_IDENT_CLAIM_NAME).asString()
    val userId: String? = decodedJWT.getClaim(OID_CLAIM_NAME).asString()
    val displayName: String? = decodedJWT.getClaim(DISPLAY_NAME_CLAIM_NAME).asString()
    val userName: String? = decodedJWT.getClaim(USERNAME_CLAIM_NAME).asString()

    companion object {
        const val NAV_IDENT_CLAIM_NAME = "NAVident"
        const val OID_CLAIM_NAME = "oid"
        const val DISPLAY_NAME_CLAIM_NAME = "name"
        const val USERNAME_CLAIM_NAME = "preferred_username"

        fun isUserPrincipal(decodedJWT: DecodedJWT): Boolean {
            val requiredClaim = decodedJWT.getClaim("NAVident")

            return !requiredClaim.isMissing && !requiredClaim.isNull
        }
    }
}
