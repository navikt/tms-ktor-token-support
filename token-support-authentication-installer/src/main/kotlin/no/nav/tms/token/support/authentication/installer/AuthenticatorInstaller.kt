package no.nav.tms.token.support.authentication.installer

import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.tms.token.support.azure.validation.AzureAuthenticatorConfig
import no.nav.tms.token.support.azure.validation.AzureInstaller.performAzureAuthenticatorInstallation
import no.nav.tms.token.support.idporten.sidecar.IdPortenInstaller.performIdPortenAuthenticatorInstallation
import no.nav.tms.token.support.idporten.sidecar.IdPortenRoutesConfig
import no.nav.tms.token.support.idporten.sidecar.IdportenAuthenticationConfig
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticatorConfig
import no.nav.tms.token.support.tokenx.validation.TokenXInstaller.performTokenXAuthenticatorInstallation
import org.slf4j.LoggerFactory

internal object AuthenticatorInstaller {
    private val log = LoggerFactory.getLogger(AuthenticatorInstaller::class.java)

    fun Application.performInstallation(config: AuthenticatorConfig) {
        checkUsage(config)
        validateDefaultToggle(config)

        var idPortenRoutesConfig: IdPortenRoutesConfig? = null

        val application = this

        install(Authentication) {

            val idPortenConfig = config.idPortenConfig

            val authContext = this

            if (idPortenConfig != null) {
                idPortenRoutesConfig = InstallerProxy.invokeIdPortenInstaller(application, idPortenConfig, authContext)
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

        setupIdPortenRoutesIfRequired(idPortenRoutesConfig)
    }

    private fun Application.setupIdPortenRoutesIfRequired(routesConfig: IdPortenRoutesConfig?) {
        routesConfig?.setupRoutes?.invoke(this)
    }

    private fun checkUsage(config: AuthenticatorConfig) {
        val numberInstalled = listOf(config.idPortenConfig, config.tokenXConfig, config.azureConfig).count { it != null }

        if (numberInstalled < 2) {
            log.info("Using the token-support-authentication-installer module is not strictly necessary when installing less than two authenticators.")
        }
    }

    private fun validateDefaultToggle(config: AuthenticatorConfig) {
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
            idPortenConfig: IdportenAuthenticationConfig,
            existingAuthContext: AuthenticationConfig): IdPortenRoutesConfig {

        return application.performIdPortenAuthenticatorInstallation(idPortenConfig, existingAuthContext)
    }

    internal fun invokeTokenXInstaller(
            application: Application,
            tokenXConfig: TokenXAuthenticatorConfig,
            existingAuthContext: AuthenticationConfig) {

        application.performTokenXAuthenticatorInstallation(tokenXConfig, existingAuthContext)
    }

    internal fun invokeAzureInstaller(
            application: Application,
            azureConfig: AzureAuthenticatorConfig,
            existingAuthContext: AuthenticationConfig) {

        application.performAzureAuthenticatorInstallation(azureConfig, existingAuthContext)
    }
}
