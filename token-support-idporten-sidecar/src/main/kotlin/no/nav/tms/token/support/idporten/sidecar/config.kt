package no.nav.tms.token.support.idporten.sidecar

import io.ktor.server.auth.*
import no.nav.tms.token.support.idporten.sidecar.install.IdPortenInstaller.performIdPortenAuthenticatorInstallation
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance.HIGH


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

    @Deprecated("Numbered login levels are deprecated as of Q4 2023. Set levelOfAssurance instead")
    var loginLevel: LoginLevel? = null
    var levelOfAssurance: LevelOfAssurance = HIGH

    var enableDefaultProxy: Boolean = false
}

enum class LevelOfAssurance {
    SUBSTANTIAL, // Equivalent to old Level3
    HIGH // Equivalent to old Level4
}

enum class LoginLevel {
    LEVEL_3, LEVEL_4
}

// Name of token authenticator. See README for example of use
object IdPortenAuthenticator {
    const val name = "idporten_access_token"
}
