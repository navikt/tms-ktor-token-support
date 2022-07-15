package no.nav.tms.token.support.tokenx.validation.mock

import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.mock.tokendings.AuthInfoValidator.validateAuthInfo
import no.nav.tms.token.support.tokenx.validation.mock.tokendings.tokenXAuthMock

object TokenXMockInstaller {
    fun Application.performTokenXMockInstallation(
        config: TokenXMockedAuthenticatorConfig,
        existingAuthContext: AuthenticationConfig? = null
    ) {
        val authenticatorName = getAuthenticatorName(config.setAsDefault)

        val authInfo = validateAuthInfo(config)

        if (existingAuthContext == null) {
            install(Authentication) {
                tokenXAuthMock(authenticatorName, authInfo)
            }
        } else {
            existingAuthContext.tokenXAuthMock(authenticatorName, authInfo)
        }
    }

    private fun getAuthenticatorName(isDefault: Boolean): String? {
        return if (isDefault) {
            null
        } else {
            TokenXAuthenticator.name
        }
    }
}
