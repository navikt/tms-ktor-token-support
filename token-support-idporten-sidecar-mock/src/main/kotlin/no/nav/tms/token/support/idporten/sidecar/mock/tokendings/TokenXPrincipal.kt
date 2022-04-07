package no.nav.tms.token.support.idporten.validation.tokendings

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.auth.*

internal data class IdportenPrincipal(val decodedJWT: DecodedJWT) : Principal
