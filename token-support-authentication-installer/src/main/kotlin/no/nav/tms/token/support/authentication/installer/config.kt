package no.nav.tms.token.support.authentication.installer

import io.ktor.application.*
import no.nav.tms.token.support.authentication.installer.AuthenticatorInstaller.performInstallation
import no.nav.tms.token.support.azure.validation.AzureAuthenticatorConfig
import no.nav.tms.token.support.idporten.IdportenAuthenticationConfig
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticatorConfig

fun Application.installAuthenticators(configure: AuthenticatorConfig.() -> Unit = {}) {
    val config = AuthenticatorConfig().also(configure)

    performInstallation(config)
}

class AuthenticatorConfig {
    internal var idPortenConfig: IdportenAuthenticationConfig? = null
    internal var tokenXConfig: TokenXAuthenticatorConfig? = null
    internal var azureConfig: AzureAuthenticatorConfig? = null

    fun installIdPortenAuth(configure: IdportenAuthenticationConfig.() -> Unit) {
        idPortenConfig = IdportenAuthenticationConfig().also(configure)
    }

    fun installTokenXAuth(configure: TokenXAuthenticatorConfig.() -> Unit) {
        tokenXConfig = TokenXAuthenticatorConfig().also(configure)
    }

    fun installAzureAuth(configure: AzureAuthenticatorConfig.() -> Unit) {
        azureConfig = AzureAuthenticatorConfig().also(configure)
    }
}

