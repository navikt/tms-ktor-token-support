package no.nav.tms.token.support.azure.validation.mock

import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.azure.validation.mock.intercept.AuthInfo
import no.nav.tms.token.support.azure.validation.mock.intercept.azureAuthMock

object AzureMockInstaller {
    fun Application.performAzureMockAuthenticatorInstallation(
        config: AzureMockedAuthenticatorConfig,
        existingAuthContext: AuthenticationConfig? = null
    ) {
        val authenticatorName = getAuthenticatorName(config.setAsDefault)

        val authInfo = AuthInfo(config.alwaysAuthenticated, config.staticJwtOverride)

        if (existingAuthContext == null) {
            install(Authentication) {
                azureAuthMock(authenticatorName, authInfo)
            }
        } else {
            existingAuthContext.azureAuthMock(authenticatorName, authInfo)
        }
    }

    private fun getAuthenticatorName(isDefault: Boolean): String? {
        return if (isDefault) {
            null
        } else {
            AzureAuthenticator.name
        }
    }
}
