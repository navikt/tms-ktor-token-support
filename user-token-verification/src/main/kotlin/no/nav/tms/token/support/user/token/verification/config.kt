package no.nav.tms.token.support.user.token.verification

import io.ktor.server.auth.*


// This method is responsible for registering the authenticators.
// Users of this library should only have to make use of this method to enable idporten auth.
fun AuthenticationConfig.userToken(
    authenticatorName: String? = null,
    configure: UserTokenAuthenticationConfig.() -> Unit
) {
    val config = UserTokenAuthenticationConfig(authenticatorName)
        .apply(configure)

    val installedVerifiers = VerifierInstaller.installVerifiers(
        requiredIssuers = config.requiredIssuers.toList(),
        minLevelOfAssurance = config.levelOfAssurance,
        webProxy = config.enableDefaultProxy
    )

    registerUserTokenAuthenticator(
        authenticatorName = config.authenticatorName,
        tokenVerifiers = installedVerifiers
    )
}


// Configuration provided by library user. See readme for example of use
class UserTokenAuthenticationConfig(
    internal val authenticatorName: String?
) {
    internal val requiredIssuers = mutableSetOf<Issuer>()
    fun configureIssuers(vararg issuer: Issuer) {
        require(issuer.isNotEmpty()) { "Må spesifisere minst én issuer" }
        requiredIssuers.addAll(issuer)
    }

    var levelOfAssurance: LevelOfAssurance = LevelOfAssurance.High

    var enableDefaultProxy: Boolean = false
}

// Name of obo issuers native to nais
enum class Issuer {
    IdPorten, Tokenx
}

// Name of token authenticator. See README for example of use
object UserTokenAuthenticator {
    const val name = "user_access_token"
}
