package no.nav.tms.token.support.azure.validation.mock

import io.ktor.server.application.*
import no.nav.tms.token.support.azure.validation.mock.AzureMockInstaller.performAzureMockAuthenticatorInstallation


fun Application.installAzureAuthMock(configure: AzureMockedAuthenticatorConfig.() -> Unit = {}) {
    val config = AzureMockedAuthenticatorConfig().also(configure)

    performAzureMockAuthenticatorInstallation(config)
}

// Configuration provided by library user. See readme for example of use
class AzureMockedAuthenticatorConfig {
    var setAsDefault: Boolean = false
    var alwaysAuthenticated: Boolean = false
    var staticJwtOverride: String? = null
}
