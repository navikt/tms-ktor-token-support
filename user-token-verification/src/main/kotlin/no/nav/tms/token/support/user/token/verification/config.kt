package no.nav.tms.token.support.user.token.verification

import io.ktor.server.auth.*
import no.nav.tms.token.support.user.token.verification.UserTokenVerificationInstaller.performUserTokenAuthenticatorInstallation


// This method is responsible for registering the authenticators.
// Users of this library should only have to make use of this method to enable idporten auth.
fun AuthenticationConfig.userToken(configure: UserTokenAuthenticationConfig.() -> Unit) =
    UserTokenAuthenticationConfig()
        .apply(configure)
        .let { performUserTokenAuthenticatorInstallation(it) }

// Configuration provided by library user. See readme for example of use
class UserTokenAuthenticationConfig {
    internal val requiredIssuers = mutableSetOf<Issuer>()
    fun requireIssuer(vararg issuer: Issuer) {
        requiredIssuers.addAll(requiredIssuers)
    }

    var authenticatorName: String = UserTokenAuthenticator.name

    var setAsDefault: Boolean = true

    var levelOfAssurance: LevelOfAssurance = LevelOfAssurance.High

    var enableDefaultProxy: Boolean = false
}

// Name of obo issuers native to nais
enum class Issuer {
    IdPorten, Tokenx
}

// Name of token authenticator. See README for example of use
object UserTokenAuthenticator {
    const val name = "idporten_access_token"
}
