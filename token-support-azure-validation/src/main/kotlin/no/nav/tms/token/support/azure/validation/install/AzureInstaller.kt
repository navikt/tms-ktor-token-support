package no.nav.tms.token.support.azure.validation.install

import io.ktor.server.auth.*
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.azure.validation.AzureAuthenticatorConfig

internal object AzureInstaller {
    fun AuthenticationConfig.performAzureAuthenticatorInstallation(
            config: AzureAuthenticatorConfig
    ) {
        registerAzureValidationProvider(
            authenticatorName = getAuthenticatorName(config),
            tokenVerifier = initializeTokenVerifier(config.enableDefaultProxy)
        )
    }

    private fun getAuthenticatorName(config: AzureAuthenticatorConfig): String? {
        return if (config.setAsDefault) {
            null
        } else {
            config.authenticatorName
        }
    }
}
