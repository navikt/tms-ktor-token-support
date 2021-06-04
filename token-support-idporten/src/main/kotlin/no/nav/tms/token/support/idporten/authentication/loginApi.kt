package no.nav.tms.token.support.idporten.authentication

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.tms.token.support.idporten.authentication.config.Idporten
import no.nav.tms.token.support.idporten.authentication.config.RuntimeContext
import no.nav.tms.token.support.idporten.user.IdportenUserFactory

internal fun Routing.loginApi(runtimeContext: RuntimeContext) {

    val verifier = createVerifier(runtimeContext)

    get("/login") {
        val redirectUri = call.redirectUri

        if (redirectUri != null) {
            call.response.cookies.append(Idporten.postLoginRedirectCookie, redirectUri, path = "/${runtimeContext.contextPath}")
        }

        call.respondRedirect(getLoginUrl(runtimeContext.contextPath))
    }

    get("/login/status") {
        val idToken = call.validIdTokenOrNull(runtimeContext.tokenCookieName, verifier)

        if (idToken == null) {
            call.respond(LoginStatus.unauthenticated())
        } else {
            call.respond(LoginStatus.authenticated(IdportenUserFactory.extractLoginLevel(idToken)))
        }
    }

    get("/refresh") {
        val idToken = call.validIdTokenOrNull(runtimeContext.tokenCookieName, verifier)

        if (idToken != null) {
            val refreshedToken = runtimeContext.tokenRefreshService.getRefreshedToken(idToken.token)

            call.respondText(refreshedToken)
        } else {
            call.respondText("No token found")
        }
    }
}

private fun ApplicationCall.validIdTokenOrNull(tokenCookieName: String, verifier: TokenVerifier): DecodedJWT? {

    val idToken = request.cookies[tokenCookieName]

    return if (idToken != null) {
        try {
            verifier.verify(idToken)
        } catch (e: Throwable) {
            null
        }
    } else {
        null
    }
}

private fun createVerifier(runtimeContext: RuntimeContext) = TokenVerifier(
        jwkProvider = runtimeContext.jwkProvider,
        clientId = runtimeContext.environment.idportenClientId,
        issuer = runtimeContext.metadata.issuer
)

private fun getLoginUrl(contextPath: String): String {
    return if (contextPath.isBlank()) {
        "/oauth2/login"
    } else {
        "/$contextPath/oauth2/login"
    }
}

private val ApplicationCall.redirectUri: String? get() = request.queryParameters["redirect_uri"]
