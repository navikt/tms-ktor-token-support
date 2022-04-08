package no.nav.tms.token.support.authentication.installer.mock

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.azure.validation.mock.AzureMockInstaller.performAzureMockAuthenticatorInstallation
import no.nav.tms.token.support.azure.validation.mock.AzureMockedAuthenticatorConfig
import no.nav.tms.token.support.idporten.sidecar.mock.IdPortenMockInstaller.performIdPortenMockInstallation
import no.nav.tms.token.support.idporten.sidecar.mock.IdPortenMockedAuthenticatorConfig
import no.nav.tms.token.support.tokenx.validation.mock.TokenXMockInstaller.performTokenXMockInstallation
import no.nav.tms.token.support.tokenx.validation.mock.TokenXMockedAuthenticatorConfig
import org.slf4j.LoggerFactory

internal object MockedAuthenticatorInstaller {
    private val log = LoggerFactory.getLogger(MockedAuthenticatorInstaller::class.java)

    fun Application.performInstallation(config: MockedAuthenticatorConfig) {
        checkUsage(config)
        validateDefaultToggle(config)

        val application = this

        install(Authentication) {

            val idPortenConfig = config.idPortenConfig

            val authContext = this

            if (idPortenConfig != null) {
                InstallerProxy.invokeIdPortenInstaller(application, idPortenConfig, authContext)
            }

            val tokenXConfig = config.tokenXConfig

            if (tokenXConfig != null) {
                InstallerProxy.invokeTokenXInstaller(application, tokenXConfig, authContext)
            }

            val azureConfig = config.azureConfig

            if (azureConfig != null) {
                InstallerProxy.invokeAzureInstaller(application, azureConfig, authContext)
            }
        }
    }

    private fun checkUsage(config: MockedAuthenticatorConfig) {
        val numberInstalled = listOf(config.idPortenConfig, config.tokenXConfig, config.azureConfig).count { it != null }

        if (numberInstalled < 2) {
            log.info("Using the token-support-authentication-installer module is not strictly necessary when installing less than two authenticators.")
        }
    }

    private fun validateDefaultToggle(config: MockedAuthenticatorConfig) {
        val numberSetAsDefault = listOf(
                config.idPortenConfig?.setAsDefault,
                config.tokenXConfig?.setAsDefault,
                config.azureConfig?.setAsDefault
        ).count { it == true }

        require(numberSetAsDefault < 2) { "At most one authenticator can be set as default." }
    }
}

// The primary purpose of this simple proxy is to enable testing without compromising on legibility too much
internal object InstallerProxy {
    internal fun invokeIdPortenInstaller(
            application: Application,
            idPortenConfig: IdPortenMockedAuthenticatorConfig,
            existingAuthContext: Authentication.Configuration) {

        return application.performIdPortenMockInstallation(idPortenConfig, existingAuthContext)
    }

    internal fun invokeTokenXInstaller(
            application: Application,
            tokenXConfig: TokenXMockedAuthenticatorConfig,
            existingAuthContext: Authentication.Configuration) {

        application.performTokenXMockInstallation(tokenXConfig, existingAuthContext)
    }

    internal fun invokeAzureInstaller(
            application: Application,
            azureConfig: AzureMockedAuthenticatorConfig,
            existingAuthContext: Authentication.Configuration) {

        application.performAzureMockAuthenticatorInstallation(azureConfig, existingAuthContext)
    }
}
