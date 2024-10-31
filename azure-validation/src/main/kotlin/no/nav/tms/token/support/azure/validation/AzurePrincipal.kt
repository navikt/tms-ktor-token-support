package no.nav.tms.token.support.azure.validation

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.auth.*

data class AzurePrincipal(val decodedJWT: DecodedJWT)
