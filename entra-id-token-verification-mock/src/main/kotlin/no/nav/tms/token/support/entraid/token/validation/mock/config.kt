package no.nav.tms.token.support.entraid.token.validation.mock

import io.ktor.server.auth.*
import no.nav.tms.token.support.entraid.validation.AzureAuthenticator
import no.nav.tms.token.support.entraid.validation.mock.install.AzureMockInstaller.performAzureMockAuthenticatorInstallation


fun AuthenticationConfig.azureMock(configure: AzureMockedAuthenticatorConfig.() -> Unit = {}) {
    val config = AzureMockedAuthenticatorConfig().also(configure)

    performAzureMockAuthenticatorInstallation(config)
}

// Configuration provided by library user. See readme for example of use
class AzureMockedAuthenticatorConfig {
    var authenticatorName: String = AzureAuthenticator.name
    var setAsDefault: Boolean = false
    var alwaysAuthenticated: Boolean = false
    var staticJwtOverride: String? = null
}
