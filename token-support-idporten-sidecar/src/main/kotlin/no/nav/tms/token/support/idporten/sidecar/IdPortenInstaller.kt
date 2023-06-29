package no.nav.tms.token.support.idporten.sidecar

import io.ktor.server.application.*
import io.ktor.server.auth.*

import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.routing.*
import no.nav.tms.token.support.idporten.sidecar.authentication.LevelOfAssuranceInternal
import no.nav.tms.token.support.idporten.sidecar.authentication.config.RuntimeContext
import no.nav.tms.token.support.idporten.sidecar.authentication.idPortenAccessToken
import no.nav.tms.token.support.idporten.sidecar.authentication.idPortenLoginApi
import no.nav.tms.token.support.idporten.sidecar.authentication.logout.idPortenLogoutApi
import org.slf4j.LoggerFactory

object IdPortenInstaller {
    private val log = LoggerFactory.getLogger(IdPortenInstaller::class.java)

    fun Application.performIdPortenAuthenticatorInstallation(
            config: IdportenAuthenticationConfig,
            existingAuthContext: AuthenticationConfig? = null
    ): IdPortenRoutesConfig {
        validateIdPortenConfig(config)

        val runtimeContext = RuntimeContext(
            postLoginRedirectUri = config.postLoginRedirectUri,
            usesRootPath = config.inheritProjectRootPath,
            contextPath = rootPath(config),
            minLevelOfAssurance = getMinLoa(config.levelOfAssurance, config.loginLevel),
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

    private fun getMinLoa(loa: LevelOfAssurance, loginLevel: LoginLevel?): LevelOfAssuranceInternal {
        return if (loginLevel != null) {
            log.warn("loginLevel will be deprecated as of Q4 2023. Use levelOfAssurance setting instead.")
            when (loginLevel) {
                LoginLevel.LEVEL_3 -> LevelOfAssuranceInternal.Substantial
                LoginLevel.LEVEL_4 -> LevelOfAssuranceInternal.High
            }
        } else {
            when (loa) {
                LevelOfAssurance.SUBSTANTIAL -> LevelOfAssuranceInternal.Substantial
                LevelOfAssurance.HIGH -> LevelOfAssuranceInternal.High
            }
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
