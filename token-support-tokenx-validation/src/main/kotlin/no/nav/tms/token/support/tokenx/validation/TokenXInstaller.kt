package no.nav.tms.token.support.tokenx.validation

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.tokenx.validation.config.RuntimeContext
import no.nav.tms.token.support.tokenx.validation.tokendings.tokenXAccessToken

object TokenXInstaller {
    fun Application.performTokenXAuthenticatorInstallation(
            config: TokenXAuthenticatorConfig,
            existingAuthContext: Authentication.Configuration? = null
    ) {
        val authenticatorName = getAuthenticatorName(config.setAsDefault)

        val runtimeContext = RuntimeContext()

        if (existingAuthContext == null) {
            install(Authentication) {
                tokenXAccessToken(authenticatorName, runtimeContext.verifierWrapper)
            }
        } else {
            existingAuthContext.tokenXAccessToken(authenticatorName, runtimeContext.verifierWrapper)
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
