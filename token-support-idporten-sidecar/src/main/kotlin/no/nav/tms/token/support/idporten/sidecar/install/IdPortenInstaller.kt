package no.nav.tms.token.support.idporten.sidecar.install

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.auth.*
import no.nav.tms.token.support.idporten.sidecar.IdportenAuthenticationConfig
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance
import no.nav.tms.token.support.idporten.sidecar.LoginLevel

internal object IdPortenInstaller {
    private val log = KotlinLogging.logger { }

    // Register authenticator for id-porten tokens
    // This can apply to any number of endpoints.
    fun AuthenticationConfig.performIdPortenAuthenticatorInstallation(
            config: IdportenAuthenticationConfig
    ) {
        val tokenVerifier = initializeTokenVerifier(
            enableDefaultProxy = config.enableDefaultProxy,
            minLevelOfAssurance = getMinLoa(config.levelOfAssurance, config.loginLevel)
        )

        registerIdPortenValidationProvider(
            authenticatorName = getAuthenticatorName(config),
            tokenVerifier = tokenVerifier
        )
    }

    private fun getMinLoa(loa: LevelOfAssurance, loginLevel: LoginLevel?): IdPortenLevelOfAssurance {
        return if (loginLevel != null) {
            log.warn { "loginLevel will be deprecated as of Q4 2023. Use levelOfAssurance setting instead." }
            when (loginLevel) {
                LoginLevel.LEVEL_3 -> IdPortenLevelOfAssurance.Substantial
                LoginLevel.LEVEL_4 -> IdPortenLevelOfAssurance.High
            }
        } else {
            when (loa) {
                LevelOfAssurance.SUBSTANTIAL -> IdPortenLevelOfAssurance.Substantial
                LevelOfAssurance.HIGH -> IdPortenLevelOfAssurance.High
            }
        }
    }

    private fun getAuthenticatorName(config: IdportenAuthenticationConfig): String? {
        return if (config.setAsDefault) {
            null
        } else {
            config.authenticatorName
        }
    }
}
