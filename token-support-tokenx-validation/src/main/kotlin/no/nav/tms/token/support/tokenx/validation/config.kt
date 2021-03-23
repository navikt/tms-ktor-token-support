package no.nav.tms.token.support.tokenx.validation

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.tokenx.validation.config.RuntimeContext
import no.nav.tms.token.support.tokenx.validation.tokendings.TokenDingsConfig
import no.nav.tms.token.support.tokenx.validation.tokendings.tokenDings


fun Application.installTokenDingsAuth(configure: IdportenAuthenticationConfig.() -> Unit) {
    val config = IdportenAuthenticationConfig().apply(configure)

    val authenticatorName = getAuthenticatorName(config.setAsDefault)

    val runtimeContext = RuntimeContext()

    install(Authentication) {
        tokenDings(authenticatorName) {
            TokenDingsConfig (
                    jwkProvider = runtimeContext.jwkProvider,
                    clientId = runtimeContext.environment.tokenxClientId,
                    issuer = runtimeContext.metadata.issuer
            )
        }

    }
}

private fun getAuthenticatorName(isDefault: Boolean): String? {
    return if (isDefault) {
        null
    } else {
        TokenDingsAuthenticator.name
    }
}

// Configuration provided by library user. See readme for example of use
class IdportenAuthenticationConfig {
    var setAsDefault: Boolean = false
}

object TokenDingsAuthenticator {
    const val name = "tokendings_bearer_token"
}
