package no.nav.tms.token.support.tokenx.validation.mock.install

import io.ktor.server.auth.*
import no.nav.tms.token.support.tokenx.validation.mock.TokenXMockedAuthenticatorConfig

internal object TokenXMockInstaller {
    fun AuthenticationConfig.performTokenXMockInstallation(
        config: TokenXMockedAuthenticatorConfig
    ) {
        registerTokenXProviderMock(
            authenticatorName = getAuthenticatorName(config),
            authInfo = AuthInfoValidator.validateAuthInfo(config)
        )
    }

    private fun getAuthenticatorName(config: TokenXMockedAuthenticatorConfig): String? {
        return if (config.setAsDefault) {
            null
        } else {
            config.authenticatorName
        }
    }
}
