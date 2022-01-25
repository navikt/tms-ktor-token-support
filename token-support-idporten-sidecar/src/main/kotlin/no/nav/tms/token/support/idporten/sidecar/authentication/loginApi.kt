package no.nav.tms.token.support.idporten.sidecar.authentication

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.tms.token.support.idporten.sidecar.authentication.config.Idporten
import no.nav.tms.token.support.idporten.sidecar.authentication.config.RuntimeContext
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUserFactory

internal fun Routing.idPortenLoginApi(runtimeContext: RuntimeContext) {

    get("/login") {
        val redirectUri = call.redirectUri

        if (redirectUri != null) {
            call.response.cookies.append(Idporten.postLoginRedirectCookie, redirectUri, path = "/${runtimeContext.contextPath}")
        }
        call.respondRedirect(getLoginUrl(runtimeContext.contextPath))
    }

    get("/login/status") {
        val idToken = call.validAccessTokenOrNull(runtimeContext.authConfiguration)

        if (idToken == null) {
            call.respond(LoginStatus.unauthenticated())
        } else {
            call.respond(LoginStatus.authenticated(IdportenUserFactory.extractLoginLevel(idToken)))
        }
    }

    get("/login/callback") {
        call.response.cookies.appendExpired(Idporten.postLoginRedirectCookie, path = "/${runtimeContext.contextPath}")
        call.respondRedirect(call.request.cookies[Idporten.postLoginRedirectCookie] ?: runtimeContext.postLoginRedirectUri)
    }
}

private fun ApplicationCall.validAccessTokenOrNull(config: AuthConfiguration): DecodedJWT? {

    val accessToken = fetchAccessToken(request, config)

    return if (accessToken != null) {
        try {
            config.tokenVerifier.verifyAccessToken(accessToken)
        } catch (e: Throwable) {
            null
        }
    } else {
        null
    }
}

private fun getLoginUrl(contextPath: String): String {
    return if (contextPath.isBlank()) {
        "/oauth2/login?redirect=/login/callback"
    } else {
        "/$contextPath/oauth2/login?redirect=$contextPath/login/callback"
    }
}

private val ApplicationCall.redirectUri: String? get() = request.queryParameters["redirect_uri"]
