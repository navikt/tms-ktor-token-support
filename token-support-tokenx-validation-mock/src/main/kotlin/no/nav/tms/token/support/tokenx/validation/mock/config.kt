package no.nav.tms.token.support.tokenx.validation.mock

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.mock.tokendings.AuthInfoValidator
import no.nav.tms.token.support.tokenx.validation.mock.tokendings.tokenXAuthMock


fun Application.installTokenXAuthMock(configure: TokenXAuthenticatorConfig.() -> Unit = {}) {
    val config = TokenXAuthenticatorConfig().also(configure)

    val authenticatorName = getAuthenticatorName(config.setAsDefault)

    val authInfo = AuthInfoValidator.validateAuthInfo(config)

    install(Authentication) {
        tokenXAuthMock(authenticatorName, authInfo)
    }
}

private fun getAuthenticatorName(isDefault: Boolean): String? {
    return if (isDefault) {
        null
    } else {
        TokenXAuthenticator.name
    }
}

enum class SecurityLevel(val claim: String) {
    LEVEL_3("Level3"),
    LEVEL_4("Level4")
}

// Configuration provided by library user. See readme for example of use
class TokenXAuthenticatorConfig {
    var setAsDefault: Boolean = false
    var alwaysAuthenticated: Boolean = false
    var staticSecurityLevel: SecurityLevel? = null
    var staticUserPid: String? = null
    var staticJwtOverride: String? = null
}
