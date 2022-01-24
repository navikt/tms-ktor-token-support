package no.nav.tms.token.support.idporten.wonderwall

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.routing.*
import no.nav.tms.token.support.idporten.wonderwall.authentication.config.RuntimeContext
import no.nav.tms.token.support.idporten.wonderwall.authentication.idPortenAccessToken
import no.nav.tms.token.support.idporten.wonderwall.authentication.idPortenLoginApi
import no.nav.tms.token.support.idporten.wonderwall.authentication.logout.idPortenLogoutApi

object IdPortenInstaller {
    fun Application.performIdPortenAuthenticatorInstallation(
            config: IdportenAuthenticationConfig,
            existingAuthContext: Authentication.Configuration? = null
    ): IdPortenRoutesConfig {
        validateIdPortenConfig(config)

        val runtimeContext = RuntimeContext(
            postLoginRedirectUri = config.postLoginRedirectUri,
            contextPath = environment.rootPath,
            loginLevel = numericLoginLevel(config.loginLevel),
            enableDefaultProxy = config.enableDefaultProxy,
            fallbackTokenCookieEnabled = config.fallbackCookieEnabled,
            fallbackTokenCookieName = config.fallbackTokenCookieName
        )

        installXForwardedHeaderSupportIfMissing()

        if (existingAuthContext == null) {
            install(Authentication) {
                setupAuthenticators(config, runtimeContext)
            }
        } else {
            existingAuthContext.setupAuthenticators(config, runtimeContext)
        }

        // Return routes to be enabled as a callback, as it as to be run after all other authenticators have been installed
        return IdPortenRoutesConfig {
            // Register endpoints '/login', '/login/status', 'login/callback' and '/logout'
            routing {
                idPortenLoginApi(runtimeContext)
                idPortenLogoutApi(runtimeContext)
            }
        }
    }

    private fun numericLoginLevel(loginLevel: LoginLevel): Int {
        return when(loginLevel) {
            LoginLevel.LEVEL_3 -> 3
            LoginLevel.LEVEL_4 -> 4
        }
    }

    private fun validateIdPortenConfig(config: IdportenAuthenticationConfig) {
        if (config.fallbackCookieEnabled) {
            require(config.fallbackTokenCookieName.isNotBlank()) { "Navn på token-cookie må spesifiseres hvis fallback er enablet." }
        }
    }

    private fun Authentication.Configuration.setupAuthenticators(config: IdportenAuthenticationConfig, runtimeContext: RuntimeContext) {
        val authenticatorName = getAuthenticatorName(config.setAsDefault)

        // Register authenticator for id-porten tokens
        // This can apply to any number of endpoints.
        idPortenAccessToken(authenticatorName, runtimeContext.authConfiguration)
    }

    private fun Application.installXForwardedHeaderSupportIfMissing() {
        if (featureOrNull(XForwardedHeaderSupport) == null) {
            install(XForwardedHeaderSupport)
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

class IdPortenRoutesConfig(val setupRoutes: Application.() -> Unit)
