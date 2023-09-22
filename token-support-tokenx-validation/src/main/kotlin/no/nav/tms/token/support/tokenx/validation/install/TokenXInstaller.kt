package no.nav.tms.token.support.tokenx.validation.install

import io.ktor.server.auth.*
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticatorConfig

internal object TokenXInstaller {
    fun AuthenticationConfig.performTokenXAuthenticatorInstallation(
            config: TokenXAuthenticatorConfig
    ) {
        val tokenVerifier = initializeTokenVerifier(
            minLevelOfAssurance = getMinLoa(config.levelOfAssurance)
        )

        registerTokenXValidatorProvider(
            authenticatorName = getAuthenticatorName(config),
            tokenVerifier = tokenVerifier
        )
    }

    private fun getAuthenticatorName(config: TokenXAuthenticatorConfig): String? {
        return if (config.setAsDefault) {
            null
        } else {
            config.authenticatorName
        }
    }

    private fun getMinLoa(loa: LevelOfAssurance): IdPortenLevelOfAssurance {
        return when (loa) {
            LevelOfAssurance.SUBSTANTIAL -> IdPortenLevelOfAssurance.Substantial
            LevelOfAssurance.HIGH -> IdPortenLevelOfAssurance.High
        }
    }
}
