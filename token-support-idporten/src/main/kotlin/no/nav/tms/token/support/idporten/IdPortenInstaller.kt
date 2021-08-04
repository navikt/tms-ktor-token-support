package no.nav.tms.token.support.idporten

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.features.*
import io.ktor.routing.*
import no.nav.tms.token.support.idporten.authentication.AuthConfiguration
import no.nav.tms.token.support.idporten.authentication.config.Idporten
import no.nav.tms.token.support.idporten.authentication.config.RuntimeContext
import no.nav.tms.token.support.idporten.authentication.idPortenAccessToken
import no.nav.tms.token.support.idporten.authentication.idPortenLoginApi
import no.nav.tms.token.support.idporten.authentication.logout.LogoutAuthenticator
import no.nav.tms.token.support.idporten.authentication.logout.LogoutConfig
import no.nav.tms.token.support.idporten.authentication.logout.idPortenLogout
import no.nav.tms.token.support.idporten.authentication.logout.idPortenLogoutApi
import no.nav.tms.token.support.idporten.authentication.oauth2LoginApi

object IdPortenInstaller {
    fun Application.performIdPortenAuthenticatorInstallation(
            config: IdportenAuthenticationConfig,
            existingAuthContext: Authentication.Configuration? = null
    ): IdPortenRoutesConfig {
        validateIdPortenConfig(config)

        val runtimeContext = RuntimeContext(
                accessTokenCookieName = config.tokenCookieName,
                tokenRefreshCookieName = config.refreshTokenCookieName,
                idTokenTokenCookieName = config.idTokenCookieName,
                contextPath = environment.rootPath,
                postLoginRedirectUri = config.postLoginRedirectUri,
                secureCookie = config.secureCookie,
                postLogoutRedirectUri = config.postLogoutRedirectUri,
                securityLevel = config.securityLevel,
                tokenRefreshMarginPercentage = config.tokenRefreshMarginPercentage
        )

        installXForwardedHeaderSupportIfMissing()

        if (existingAuthContext == null) {
            install(Authentication) {
                setupAuthenticators(config, runtimeContext)
            }
        } else {
            existingAuthContext.setupAuthenticators(config,runtimeContext)
        }

        // Return routes to be enabled as a callback, as it as to be run after all other authenticators have been installed
        return IdPortenRoutesConfig {
            // Register endpoints '/login', '/login/status', 'oauth2/login', 'oath2/callback', '/logout', and /oauth2/logout
            routing {
                idPortenLoginApi(runtimeContext)
                oauth2LoginApi(runtimeContext)
                idPortenLogoutApi(runtimeContext)
            }
        }
    }

    private fun validateIdPortenConfig(config: IdportenAuthenticationConfig) {
        require(config.tokenCookieName.isNotBlank()) { "Navn på token-cookie må spesifiseres." }

        require(config.postLogoutRedirectUri.isNotBlank()) { "Post-logout uri må spesifiseres. Pass på at dette matcher nais yaml." }

        require(config.tokenRefreshMarginPercentage in 0..100) {
            "tokenRefreshMarginPercentage må være mellom inklusive 0 og 100."
        }
    }

    private fun Authentication.Configuration.setupAuthenticators(config: IdportenAuthenticationConfig, runtimeContext: RuntimeContext) {
        val authenticatorName = getAuthenticatorName(config.setAsDefault)

        // Register authenticator which redirects to internal oauth2/login endpoint if user does not have a valid token.
        // This can apply to any number of endpoints.
        idPortenAccessToken(authenticatorName) {
            AuthConfiguration (
                    jwkProvider = runtimeContext.jwkProvider,
                    contextPath = runtimeContext.contextPath,
                    accessTokenCookieName = config.tokenCookieName,
                    refreshTokenCookieName = runtimeContext.tokenRefreshCookieName,
                    clientId = runtimeContext.environment.idportenClientId,
                    issuer = runtimeContext.metadata.issuer,
                    shouldRedirect = config.alwaysRedirectToLogin,
                    shouldRefreshToken = config.tokenRefreshEnabled,
                    tokenRefreshService = runtimeContext.tokenRefreshService,
                    secureCookie = config.secureCookie
            )
        }

        // Register authenticator which redirects user to idporten to perform login. This should only apply to endpoints
        // 'oath2/login' and 'oath2/callback'
        oauth(Idporten.authenticatorName) {
            client = HttpClient(Apache)
            providerLookup = { runtimeContext.oauth2ServerSettings }
            urlProvider = { runtimeContext.environment.idportenRedirectUri }
        }

        // Register endpoints for performing logout. This includes an endpoint which initiates single logout through
        // ID-porten, and one which handles logout initiated elsewhere
        idPortenLogout(LogoutAuthenticator.name) {
            LogoutConfig(
                    idTokenCookieName = runtimeContext.idTokenTokenCookieName,
            )
        }
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
