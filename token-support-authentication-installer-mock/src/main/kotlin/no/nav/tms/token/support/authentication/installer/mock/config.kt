package no.nav.tms.token.support.authentication.installer.mock

import io.ktor.application.*
import no.nav.tms.token.support.authentication.installer.mock.MockedAuthenticatorInstaller.performInstallation
import no.nav.tms.token.support.azure.validation.mock.AzureMockedAuthenticatorConfig
import no.nav.tms.token.support.idporten.sidecar.mock.IdPortenMockedAuthenticatorConfig
import no.nav.tms.token.support.tokenx.validation.mock.TokenXMockedAuthenticatorConfig

fun Application.installMockedAuthenticators(configure: MockedAuthenticatorConfig.() -> Unit = {}) {
    val config = MockedAuthenticatorConfig().also(configure)

    performInstallation(config)
}

class MockedAuthenticatorConfig {
    internal var idPortenConfig: IdPortenMockedAuthenticatorConfig? = null
    internal var tokenXConfig: TokenXMockedAuthenticatorConfig? = null
    internal var azureConfig: AzureMockedAuthenticatorConfig? = null

    fun installIdPortenAuthMock(configure: IdPortenMockedAuthenticatorConfig.() -> Unit) {
        idPortenConfig = IdPortenMockedAuthenticatorConfig().also(configure)
    }

    fun installTokenXAuthMock(configure: TokenXMockedAuthenticatorConfig.() -> Unit) {
        tokenXConfig = TokenXMockedAuthenticatorConfig().also(configure)
    }

    fun installAzureAuthMock(configure: AzureMockedAuthenticatorConfig.() -> Unit) {
        azureConfig = AzureMockedAuthenticatorConfig().also(configure)
    }
}

