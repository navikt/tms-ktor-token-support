package no.nav.tms.token.support.user.token.verification

import io.ktor.server.auth.*

internal object UserTokenVerificationInstaller {

    // Register authenticator for id-porten tokens
    // This can apply to any number of endpoints.
    fun AuthenticationConfig.performUserTokenAuthenticatorInstallation(
        config: UserTokenAuthenticationConfig
    ) {
        val installedVerifiers = VerifierInstaller.installVerifiers(
            requiredIssuers = config.requiredIssuers.toList(),
            minLevelOfAssurance = config.levelOfAssurance,
            webProxy = config.enableDefaultProxy
        )

        registerUserTokenAuthenticator(
            authenticatorName = getAuthenticatorName(config),
            tokenVerifiers = installedVerifiers
        )
    }

    private fun getAuthenticatorName(config: UserTokenAuthenticationConfig): String? {
        return if (config.setAsDefault) {
            null
        } else {
            config.authenticatorName
        }
    }
}
