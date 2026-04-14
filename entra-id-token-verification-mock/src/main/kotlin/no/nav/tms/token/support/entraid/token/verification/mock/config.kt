package no.nav.tms.token.support.entraid.token.verification.mock

import io.ktor.server.auth.*
import no.nav.tms.token.support.entraid.token.verification.mock.install.AuthInfo
import no.nav.tms.token.support.entraid.token.verification.mock.install.registerAzureProviderMock


fun AuthenticationConfig.azureMock(authenticatorName: String? = null, configure: AzureMockedAuthenticatorConfig.() -> Unit = {}) {
    val config = AzureMockedAuthenticatorConfig().also(configure)

    registerAzureProviderMock(
        authenticatorName = authenticatorName,
        authInfo = AuthInfo(config.alwaysAuthenticated, config.staticJwtOverride)
    )
}

// Configuration provided by library user. See readme for example of use
class AzureMockedAuthenticatorConfig {
    var setAsDefault: Boolean = false
    var alwaysAuthenticated: Boolean = false
    var staticJwtOverride: String? = null
}
