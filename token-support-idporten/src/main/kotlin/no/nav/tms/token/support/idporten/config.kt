package no.nav.tms.token.support.idporten

import io.ktor.application.*
import no.nav.tms.token.support.idporten.IdPortenInstaller.performIdPortenAuthenticatorInstallation
import no.nav.tms.token.support.idporten.SecurityLevel.NOT_SPECIFIED


// This method is responsible for wiring up all the necessary endpoints and registering the authenticators.
// Users of this library should only have to make use of this method to enable idporten auth.
fun Application.installIdPortenAuth(configure: IdportenAuthenticationConfig.() -> Unit) {
    val config = IdportenAuthenticationConfig().apply(configure)

    performIdPortenAuthenticatorInstallation(config)
}

// Configuration provided by library user. See readme for example of use
class IdportenAuthenticationConfig {
    var tokenCookieName: String = ""
    var postLoginRedirectUri: String = ""
    var setAsDefault: Boolean = false
    var secureCookie: Boolean = true
    var postLogoutRedirectUri: String = ""
    var alwaysRedirectToLogin: Boolean = false
    var securityLevel: SecurityLevel = NOT_SPECIFIED
    var tokenRefreshEnabled: Boolean = false
    var tokenRefreshMarginPercentage: Int = 25

    val refreshTokenCookieName get() = "${tokenCookieName}_refresh_token"
    val idTokenCookieName get() = "${tokenCookieName}_id_token"
}

enum class SecurityLevel {
    NOT_SPECIFIED, LEVEL_3, LEVEL_4
}

// Name of token authenticator. See README for example of use
object IdPortenCookieAuthenticator {
    const val name = "idporten_cookie"
}
