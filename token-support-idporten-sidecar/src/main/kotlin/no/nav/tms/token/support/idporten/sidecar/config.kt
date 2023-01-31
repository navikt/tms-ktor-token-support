package no.nav.tms.token.support.idporten.sidecar

import io.ktor.server.application.*
import no.nav.tms.token.support.idporten.sidecar.IdPortenInstaller.performIdPortenAuthenticatorInstallation
import no.nav.tms.token.support.idporten.sidecar.LoginLevel.LEVEL_4

// This method is responsible for wiring up all the necessary endpoints and registering the authenticators.
// Users of this library should only have to make use of this method to enable idporten auth.
fun Application.installIdPortenAuth(configure: IdportenAuthenticationConfig.() -> Unit) {
    val config = IdportenAuthenticationConfig().apply(configure)

    val routesConfig = performIdPortenAuthenticatorInstallation(config)

    routesConfig.setupRoutes(this)
}

// Configuration provided by library user. See readme for example of use
class IdportenAuthenticationConfig {
    var setAsDefault: Boolean = false
    var postLoginRedirectUri: String = ""
    var loginLevel: LoginLevel = LEVEL_4
    var enableDefaultProxy: Boolean = false

    var inheritProjectRootPath: Boolean = false
    var rootPath: String = ""

    var fallbackCookieEnabled: Boolean = false
    var fallbackTokenCookieName: String = ""
}

enum class LoginLevel {
    LEVEL_3, LEVEL_4
}

// Name of token authenticator. See README for example of use
object IdPortenCookieAuthenticator {
    const val name = "idporten_wonderwall"
}
