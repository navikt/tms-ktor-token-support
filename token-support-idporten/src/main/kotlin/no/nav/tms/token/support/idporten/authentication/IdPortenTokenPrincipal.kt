package no.nav.tms.token.support.idporten.authentication

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.auth.*

internal data class IdPortenTokenPrincipal(
        val accessToken: DecodedJWT,
        val refreshToken: String
) : Principal

