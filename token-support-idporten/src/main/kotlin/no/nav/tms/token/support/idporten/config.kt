package no.nav.tms.token.support.idporten

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.features.*
import io.ktor.routing.*
import no.nav.tms.token.support.idporten.SecurityLevel.NOT_SPECIFIED
import no.nav.tms.token.support.idporten.authentication.AuthConfiguration
import no.nav.tms.token.support.idporten.authentication.config.Idporten
import no.nav.tms.token.support.idporten.authentication.config.RuntimeContext
import no.nav.tms.token.support.idporten.authentication.accessToken
import no.nav.tms.token.support.idporten.authentication.loginApi
import no.nav.tms.token.support.idporten.authentication.oauth2LoginApi
import no.nav.tms.token.support.idporten.authentication.logout.LogoutAuthenticator
import no.nav.tms.token.support.idporten.authentication.logout.LogoutConfig
import no.nav.tms.token.support.idporten.authentication.logout.idPortenLogout
import no.nav.tms.token.support.idporten.authentication.logout.logoutApi


// This method is responsible for wiring up all the necessary endpoints and registering the authenticators.
// Users of this library should only have to make use of this method to enable idporten auth.
fun Application.installIdPortenAuth(configure: IdportenAuthenticationConfig.() -> Unit) {
    val config = IdportenAuthenticationConfig().apply(configure)
    val contextPath = environment.rootPath
    val cookieName = config.tokenCookieName
    val postLogoutRedirectUri = config.postLogoutRedirectUri
    val shouldRedirect = config.alwaysRedirectToLogin

    val authenticatorName = getAuthenticatorName(config.setAsDefault)

    require(cookieName.isNotBlank()) { "Navn på token-cookie må spesifiseres." }

    require(postLogoutRedirectUri.isNotBlank()) { "Post-logout uri må spesifiseres. Pass på at dette matcher nais yaml." }

    require(config.tokenRefreshMarginPercentage in 0..100) {
        "tokenRefreshMarginPercentage må være mellom inklusive 0 og 100."
    }

    val runtimeContext = RuntimeContext(
            accessTokenCookieName = cookieName,
            tokenRefreshCookieName = config.refreshTokenCookieName,
            idTokenTokenCookieName = config.idTokenCookieName,
            contextPath = contextPath,
            postLoginRedirectUri = config.postLoginRedirectUri,
            secureCookie = config.secureCookie,
            postLogoutRedirectUri = postLogoutRedirectUri,
            securityLevel = config.securityLevel,
            tokenRefreshMarginPercentage = config.tokenRefreshMarginPercentage
    )

    installXForwardedHeaderSupportIfMissing()

    install(Authentication) {
        // Register authenticator which redirects to internal oauth2/login endpoint if user does not have a valid token.
        // This can apply to any number of endpoints.
        accessToken(authenticatorName) {
            AuthConfiguration (
                    jwkProvider = runtimeContext.jwkProvider,
                    contextPath = contextPath,
                    accessTokenCookieName = cookieName,
                    refreshTokenCookieName = runtimeContext.tokenRefreshCookieName,
                    clientId = runtimeContext.environment.idportenClientId,
                    issuer = runtimeContext.metadata.issuer,
                    shouldRedirect = shouldRedirect,
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

    // Register endpoints '/login', '/login/status', 'oauth2/login', 'oath2/callback', '/logout', and /oauth2/logout
    routing {
        loginApi(runtimeContext)
        oauth2LoginApi(runtimeContext)
        logoutApi(runtimeContext)
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

// Configuration provided by library user. See readme for example of use
class IdportenAuthenticationConfig {
    var tokenCookieName: String = ""
    var postLoginRedirectUri: String = ""
    var setAsDefault: Boolean = false
    var secureCookie: Boolean = true
    var postLogoutRedirectUri: String = ""
    var alwaysRedirectToLogin: Boolean = false
    var securityLevel: SecurityLevel = NOT_SPECIFIED
    var tokenRefreshEnabled: Boolean = false
    var tokenRefreshMarginPercentage: Int = 25

    val refreshTokenCookieName get() = "${tokenCookieName}_refresh_token"
    val idTokenCookieName get() = "${tokenCookieName}_id_token"
}

enum class SecurityLevel {
    NOT_SPECIFIED, LEVEL_3, LEVEL_4
}

// Name of token authenticator. See README for example of use
object IdPortenCookieAuthenticator {
    const val name = "idporten_cookie"
}
