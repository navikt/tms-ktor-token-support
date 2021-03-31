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
import no.nav.tms.token.support.idporten.authentication.idToken
import no.nav.tms.token.support.idporten.authentication.loginApi
import no.nav.tms.token.support.idporten.authentication.logout.LogoutAuthenticator
import no.nav.tms.token.support.idporten.authentication.logout.LogoutConfig
import no.nav.tms.token.support.idporten.authentication.logout.idTokenLogout
import no.nav.tms.token.support.idporten.authentication.logout.logoutApi


// This method is responsible for wiring up all the necessary endpoints and registering the authenticators.
// Users of this library should only have to make use of this method to enable idporten auth.
fun Application.installIdPortenAuth(configure: IdportenAuthenticationConfig.() -> Unit) {
    val config = IdportenAuthenticationConfig().apply(configure)
    val contextPath = environment.rootPath
    val cookieName = config.tokenCookieName
    val postLogoutRedirectUri = config.postLogoutRedirectUri

    val authenticatorName = getAuthenticatorName(config.setAsDefault)

    require(cookieName.isNotBlank()) { "Navn på token-cookie må spesifiseres." }

    require(postLogoutRedirectUri.isNotBlank()) { "Post-logout uri må spesifiseres. Pass på at dette matcher nais yaml." }

    val runtimeContext = RuntimeContext(
            tokenCookieName = cookieName,
            contextPath = contextPath,
            postLoginRedirectUri = config.postLoginRedirectUri,
            secureCookie = config.secureCookie,
            postLogoutRedirectUri = postLogoutRedirectUri
    )

    installXForwardedHeaderSupportIfMissing()

    install(Authentication) {
        // Register authenticator which redirects to internal oauth2/login endpoint if user does not have a valid token.
        // This can apply to any number of endpoints.
        idToken(authenticatorName) {
            AuthConfiguration (
                    jwkProvider = runtimeContext.jwkProvider,
                    contextPath = contextPath,
                    tokenCookieName = cookieName,
                    clientId = runtimeContext.environment.idportenClientId,
                    issuer = runtimeContext.metadata.issuer
            )
        }

        // Register authenticator which redirects user to idporten to perform login. This should only apply to endpoints
        // 'oath2/login' and 'oath2/callback'
        oauth(Idporten.authenticatorName) {
            client = HttpClient(Apache)
            providerLookup = { runtimeContext.oauth2ServerSettings }
            urlProvider = { runtimeContext.environment.idportenRedirectUri }
        }

        idTokenLogout(LogoutAuthenticator.name) {
            LogoutConfig(tokenCookieName = cookieName)
        }

    }

    // Register endpoints 'oauth2/login' and 'oath2/callback'
    routing {
        loginApi(runtimeContext)
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
}

// Name of token authenticator. See README for example of use
object IdPortenCookieAuthenticator {
    const val name = "idporten_cookie"
}
