package no.nav.tms.token.support.idporten.sidecar

import io.ktor.server.auth.*
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance.HIGH
import no.nav.tms.token.support.idporten.sidecar.install.IdPortenInstaller.performIdPortenAuthenticatorInstallation


// This method is responsible for registering the authenticators.
// Users of this library should only have to make use of this method to enable idporten auth.
fun AuthenticationConfig.idPorten(configure: IdportenAuthenticationConfig.() -> Unit) =
    IdportenAuthenticationConfig()
        .apply(configure)
        .let { performIdPortenAuthenticatorInstallation(it) }

// Configuration provided by library user. See readme for example of use
class IdportenAuthenticationConfig {
    var authenticatorName: String = IdPortenAuthenticator.name

    var setAsDefault: Boolean = false

    var levelOfAssurance: LevelOfAssurance = HIGH

    var enableDefaultProxy: Boolean = false
}

enum class LevelOfAssurance {
    SUBSTANTIAL, // Equivalent to old Level3
    HIGH // Equivalent to old Level4
}

// Name of token authenticator. See README for example of use
object IdPortenAuthenticator {
    const val name = "idporten_access_token"
}
