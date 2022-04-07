package no.nav.tms.token.support.idporten.validation.mock

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.idporten.validation.IdportenAuthenticator
import no.nav.tms.token.support.idporten.validation.mock.tokendings.AuthInfoValidator.validateAuthInfo
import no.nav.tms.token.support.idporten.validation.mock.tokendings.idportenAuthMock

object IdportenMockInstaller {
    fun Application.performIdportenMockInstallation(
            config: IdportenAuthenticatorConfig,
            existingAuthContext: Authentication.Configuration? = null
    ) {
        val authenticatorName = getAuthenticatorName(config.setAsDefault)

        val authInfo = validateAuthInfo(config)

        if (existingAuthContext == null) {
            install(Authentication) {
                idportenAuthMock(authenticatorName, authInfo)
            }
        } else {
            existingAuthContext.idportenAuthMock(authenticatorName, authInfo)
        }
    }

    private fun getAuthenticatorName(isDefault: Boolean): String? {
        return if (isDefault) {
            null
        } else {
            IdportenAuthenticator.name
        }
    }
}
