package no.nav.tms.token.support.user.token.verification

import io.ktor.server.auth.*
import no.nav.tms.token.support.idporten.sidecar.IdportenAuthenticationConfig

internal object UserTokenVerificationInstaller {

    // Register authenticator for id-porten tokens
    // This can apply to any number of endpoints.
    fun AuthenticationConfig.performIdPortenAuthenticatorInstallation(
            config: UserTokenAuthenticationConfig
    ) {
        val tokenVerifier = initializeTokenVerifier(
            enableDefaultProxy = config.enableDefaultProxy,
            minLevelOfAssurance = getMinLoa(config.levelOfAssurance)
        )

        registerIdPortenValidationProvider(
            authenticatorName = getAuthenticatorName(config),
            tokenVerifier = tokenVerifier
        )
    }

    private fun getMinLoa(loa: LevelOfAssurance) = when (loa) {
        LevelOfAssurance.SUBSTANTIAL -> IdPortenLevelOfAssurance.Substantial
        LevelOfAssurance.HIGH -> IdPortenLevelOfAssurance.High
    }

    private fun getAuthenticatorName(config: IdportenAuthenticationConfig): String? {
        return if (config.setAsDefault) {
            null
        } else {
            config.authenticatorName
        }
    }
}
