package no.nav.tms.token.support.idporten.sidecar.mock

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.idporten.sidecar.IdPortenCookieAuthenticator
import no.nav.tms.token.support.idporten.sidecar.mock.authentication.AuthInfoValidator
import no.nav.tms.token.support.idporten.sidecar.mock.authentication.idPortenAuthMock


fun Application.installIdPortenAuthMock(configure: IdPortenMockedAuthenticatorConfig.() -> Unit = {}) {
    val config = IdPortenMockedAuthenticatorConfig().also(configure)

    val authenticatorName = getAuthenticatorName(config.setAsDefault)

    val authInfo = AuthInfoValidator.validateAuthInfo(config)

    install(Authentication) {
        idPortenAuthMock(authenticatorName, authInfo)
    }
}

private fun getAuthenticatorName(isDefault: Boolean): String? {
    return if (isDefault) {
        null
    } else {
        IdPortenCookieAuthenticator.name
    }
}

enum class SecurityLevel(val claim: String) {
    LEVEL_3("Level3"),
    LEVEL_4("Level4")
}

// Configuration provided by library user. See readme for example of use
class IdPortenMockedAuthenticatorConfig {
    var setAsDefault: Boolean = false
    var alwaysAuthenticated: Boolean = false
    var staticSecurityLevel: SecurityLevel? = null
    var staticUserPid: String? = null
    var staticJwtOverride: String? = null
}
