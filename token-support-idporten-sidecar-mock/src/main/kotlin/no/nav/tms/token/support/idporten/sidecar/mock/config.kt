package no.nav.tms.token.support.idporten.validation.mock

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.idporten.validation.IdportenAuthenticator
import no.nav.tms.token.support.idporten.validation.mock.tokendings.AuthInfoValidator
import no.nav.tms.token.support.idporten.validation.mock.tokendings.idportenAuthMock


fun Application.installIdportenAuthMock(configure: IdportenAuthenticatorConfig.() -> Unit = {}) {
    val config = IdportenAuthenticatorConfig().also(configure)

    val authenticatorName = getAuthenticatorName(config.setAsDefault)

    val authInfo = AuthInfoValidator.validateAuthInfo(config)

    install(Authentication) {
        idportenAuthMock(authenticatorName, authInfo)
    }
}

private fun getAuthenticatorName(isDefault: Boolean): String? {
    return if (isDefault) {
        null
    } else {
        IdportenAuthenticator.name
    }
}

enum class SecurityLevel(val claim: String) {
    LEVEL_3("Level3"),
    LEVEL_4("Level4")
}

// Configuration provided by library user. See readme for example of use
class IdportenAuthenticatorConfig {
    var setAsDefault: Boolean = false
    var alwaysAuthenticated: Boolean = false
    var staticSecurityLevel: SecurityLevel? = null
    var staticUserPid: String? = null
    var staticJwtOverride: String? = null
}
