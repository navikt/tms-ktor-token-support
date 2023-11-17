package no.nav.tms.token.support.azure.validation.mock.install

import io.ktor.server.auth.*
import no.nav.tms.token.support.azure.validation.mock.AzureMockedAuthenticatorConfig

internal object AzureMockInstaller {
    fun AuthenticationConfig.performAzureMockAuthenticatorInstallation(
        config: AzureMockedAuthenticatorConfig,
    ) {
        registerAzureProviderMock(
            authenticatorName = getAuthenticatorName(config),
            authInfo = AuthInfo(config.alwaysAuthenticated, config.staticJwtOverride)
        )
    }

    private fun getAuthenticatorName(config: AzureMockedAuthenticatorConfig): String? {
        return if (config.setAsDefault) {
            null
        } else {
            config.authenticatorName
        }
    }
}
