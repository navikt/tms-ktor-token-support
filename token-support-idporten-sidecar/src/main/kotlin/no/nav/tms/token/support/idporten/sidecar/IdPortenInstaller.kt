package no.nav.tms.token.support.idporten.sidecar

import io.ktor.server.application.*
import io.ktor.server.auth.*

import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.routing.*
import no.nav.tms.token.support.idporten.sidecar.authentication.config.RuntimeContext
import no.nav.tms.token.support.idporten.sidecar.authentication.idPortenAccessToken
import no.nav.tms.token.support.idporten.sidecar.authentication.idPortenLoginApi
import no.nav.tms.token.support.idporten.sidecar.authentication.logout.idPortenLogoutApi

object IdPortenInstaller {
    fun Application.performIdPortenAuthenticatorInstallation(
            config: IdportenAuthenticationConfig,
            existingAuthContext: AuthenticationConfig? = null
    ): IdPortenRoutesConfig {
        validateIdPortenConfig(config)

        val runtimeContext = RuntimeContext(
            postLoginRedirectUri = config.postLoginRedirectUri,
            usesRootPath = config.inheritProjectRootPath,
            contextPath = rootPath(config),
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

    private fun Application.rootPath(config: IdportenAuthenticationConfig): String {
        return if (config.inheritProjectRootPath) {
            environment.rootPath
        } else {
            config.rootPath
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

    private fun AuthenticationConfig.setupAuthenticators(config: IdportenAuthenticationConfig, runtimeContext: RuntimeContext) {
        val authenticatorName = getAuthenticatorName(config.setAsDefault)

        // Register authenticator for id-porten tokens
        // This can apply to any number of endpoints.
        idPortenAccessToken(authenticatorName, runtimeContext.authConfiguration)
    }

    private fun Application.installXForwardedHeaderSupportIfMissing() {
        if (pluginOrNull(XForwardedHeaders) == null) {
            install(XForwardedHeaders)
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
