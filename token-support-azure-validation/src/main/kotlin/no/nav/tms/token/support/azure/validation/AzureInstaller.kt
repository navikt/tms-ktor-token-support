package no.nav.tms.token.support.azure.validation

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.azure.validation.config.RuntimeContext
import no.nav.tms.token.support.azure.validation.intercept.azureAccessToken

object AzureInstaller {
    fun Application.performAzureAuthenticatorInstallation(
            config: AzureAuthenticatorConfig,
            existingAuthContext: Authentication.Configuration? = null
    ) {
        val authenticatorName = getAuthenticatorName(config.setAsDefault)

        val runtimeContext = RuntimeContext(config.enableDefaultProxy)

        if (existingAuthContext == null) {
            install(Authentication) {
                azureAccessToken(authenticatorName, runtimeContext.verifierWrapper)
            }
        } else {
            existingAuthContext.azureAccessToken(authenticatorName, runtimeContext.verifierWrapper)
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
