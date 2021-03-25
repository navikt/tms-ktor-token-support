package no.nav.tms.token.support.tokenx.validation

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.tokenx.validation.config.RuntimeContext
import no.nav.tms.token.support.tokenx.validation.tokendings.tokenX


fun Application.installTokenXAuth(configure: TokenXAuthenticatorConfig.() -> Unit = {}) {
    val config = TokenXAuthenticatorConfig().also(configure)

    val authenticatorName = getAuthenticatorName(config.setAsDefault)

    val runtimeContext = RuntimeContext()

    install(Authentication) {
        tokenX(authenticatorName, runtimeContext.verifierWrapper)
    }
}

private fun getAuthenticatorName(isDefault: Boolean): String? {
    return if (isDefault) {
        null
    } else {
        TokenXAuthenticator.name
    }
}

// Configuration provided by library user. See readme for example of use
class TokenXAuthenticatorConfig {
    var setAsDefault: Boolean = false
}

object TokenXAuthenticator {
    const val name = "tokenx_bearer_access_token"
}
