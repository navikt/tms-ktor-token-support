package no.nav.tms.token.support.tokenx.validation.tokendings

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.auth.*

internal data class TokenDingsPrincipal(val decodedJWT: DecodedJWT) : Principal
