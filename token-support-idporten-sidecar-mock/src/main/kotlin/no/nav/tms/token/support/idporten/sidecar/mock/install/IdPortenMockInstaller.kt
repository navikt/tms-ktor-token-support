package no.nav.tms.token.support.idporten.sidecar.mock.install

import io.ktor.server.auth.*
import no.nav.tms.token.support.idporten.sidecar.mock.IdPortenMockedAuthenticatorConfig

internal object IdPortenMockInstaller {
    fun AuthenticationConfig.performIdPortenMockInstallation(
        config: IdPortenMockedAuthenticatorConfig
    ) {
        registerIdPortenProviderMock(
            authenticatorName = getAuthenticatorName(config),
            authInfo = AuthInfoValidator.validateAuthInfo(config)
        )
    }

    private fun getAuthenticatorName(config: IdPortenMockedAuthenticatorConfig): String? {
        return if (config.setAsDefault) {
            null
        } else {
            config.authenticatorName
        }
    }
}
