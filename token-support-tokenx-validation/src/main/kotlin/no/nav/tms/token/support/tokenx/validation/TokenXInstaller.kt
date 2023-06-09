package no.nav.tms.token.support.tokenx.validation

import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.tms.token.support.tokenx.validation.config.RuntimeContext
import no.nav.tms.token.support.tokenx.validation.tokendings.LevelOfAssuranceInternal
import no.nav.tms.token.support.tokenx.validation.tokendings.tokenXAccessToken

object TokenXInstaller {
    fun Application.performTokenXAuthenticatorInstallation(
            config: TokenXAuthenticatorConfig,
            existingAuthContext: AuthenticationConfig? = null
    ) {
        val authenticatorName = getAuthenticatorName(config.setAsDefault)

        val runtimeContext = RuntimeContext(
            minLevelOfAssurance = getMinLoa(config.levelOfAssurance)
        )

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

    private fun getMinLoa(loa: LevelOfAssurance): LevelOfAssuranceInternal {
        return when (loa) {
            LevelOfAssurance.SUBSTANTIAL -> LevelOfAssuranceInternal.Substantial
            LevelOfAssurance.HIGH -> LevelOfAssuranceInternal.High
        }
    }
}
