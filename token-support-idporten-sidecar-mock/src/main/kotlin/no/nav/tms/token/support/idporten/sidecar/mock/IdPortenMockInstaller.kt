package no.nav.tms.token.support.idporten.sidecar.mock

import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.tms.token.support.idporten.sidecar.IdPortenCookieAuthenticator
import no.nav.tms.token.support.idporten.sidecar.mock.authentication.AuthInfoValidator.validateAuthInfo
import no.nav.tms.token.support.idporten.sidecar.mock.authentication.idPortenAuthMock

object IdPortenMockInstaller {
    fun Application.performIdPortenMockInstallation(
        config: IdPortenMockedAuthenticatorConfig,
        existingAuthContext: AuthenticationConfig? = null
    ) {
        val authenticatorName = getAuthenticatorName(config.setAsDefault)

        val authInfo = validateAuthInfo(config)

        if (existingAuthContext == null) {
            install(Authentication) {
                idPortenAuthMock(authenticatorName, authInfo)
            }
        } else {
            existingAuthContext.idPortenAuthMock(authenticatorName, authInfo)
        }
    }

    private fun getAuthenticatorName(isDefault: Boolean): String? {
        return if (isDefault) {
            null
        } else {
            IdPortenCookieAuthenticator.name
        }
    }
}
