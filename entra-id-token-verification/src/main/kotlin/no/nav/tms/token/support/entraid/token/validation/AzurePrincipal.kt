package no.nav.tms.token.support.entraid.token.validation

import com.auth0.jwt.interfaces.DecodedJWT

data class AzurePrincipal(val decodedJWT: DecodedJWT)
