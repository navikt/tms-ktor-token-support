package no.nav.tms.token.support.user.login.routes

import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.date.*

private const val postLoginRedirectCookie = "redirect_uri"

internal fun Routing.idPortenLoginApi(tokenVerifier: TokenVerifier, rootpath: String, routesPrefix: String?) {

    if (routesPrefix != null) {
        route("/$routesPrefix") {
            loginEndPoints(tokenVerifier, rootpath, routesPrefix)
        }
    } else {
        loginEndPoints(tokenVerifier, rootpath, routesPrefix)
    }
}

private fun Route.loginEndPoints(tokenVerifier: TokenVerifier, rootpath: String, routesPrefix: String?) {

    get("/login") {
        call.redirectUri?.let { redirectUri ->
            if (isValidCallbackUrl(redirectUri)) {
                call.response.cookies.append(postLoginRedirectCookie, redirectUri)
            } else {
                call.respond(HttpStatusCode.UnprocessableEntity, "Erroneous callback-url. Must lead to 'nav.no' domain")
            }
        }

        findRelativePath(rootpath, routesPrefix)
            .let { relativePath -> getLoginUrl(relativePath, call.levelOfAssurance) }
            .let { loginUrl -> call.respondRedirect(loginUrl) }
    }

    get("/login/status") {
        val idToken = call.validAccessTokenOrNull(tokenVerifier)

        if (idToken == null) {
            call.respondJson(LoginStatus.unauthenticated())
        } else {
            val loa = idToken.getClaim("acr").let { acr ->
                IdPortenLevelOfAssurance.fromAcr(acr.asString())
            }

            call.respondJson(LoginStatus.authenticated(loa))
        }
    }

    get("/login/callback") {
        call.response.cookies.append(
            name = postLoginRedirectCookie,
            value = "",
            expires = GMTDate.START
        )

        val callbackUrl = call.request.cookies[postLoginRedirectCookie]

        if (callbackUrl == null) {
            call.respond(HttpStatusCode.OK, "Login successful")
        } else if (isValidCallbackUrl(callbackUrl)) {
            call.respondRedirect(callbackUrl)
        } else {
            call.respond(HttpStatusCode.UnprocessableEntity, "Erroneous url in callback-cookie")
        }
    }
}

private fun getLoginUrl(relativePath: String, levelOfAssurance: String?): String {

    val redirectPath = "${relativePath}/oauth2/login?redirect=${relativePath}/login/callback"

    return if (levelOfAssurance != null) {
        "$redirectPath&level=$levelOfAssurance"
    } else {
        redirectPath
    }
}

private fun findRelativePath(rootpath: String, prefix: String?): String {
    val rootPathPart = when {
        rootpath.isStub() -> ""
        else -> rootpath.trim('/')
    }

    val prefixPart = when {
        prefix == null -> ""
        prefix.isStub() -> ""
        else -> prefix.trim('/')
    }

    return when {
        rootPathPart.isBlank() && prefixPart.isBlank() -> ""
        prefixPart.isBlank() -> "/$rootPathPart"
        rootPathPart.isBlank() -> "/$prefixPart"
        else -> "/$rootPathPart/$prefixPart"
    }
}


// Callback must lead to a nav.no-domain
private val navDomainPattern = "https://(?:[a-z0-9-]{0,61}\\.)*nav\\.no(\\z|[/?])".toRegex()
private fun isValidCallbackUrl(callbackUrl: String): Boolean {
    return navDomainPattern.containsMatchIn(callbackUrl)
}

private fun String.isStub() = when(this) {
    "" -> true
    "/" -> true
    else -> false
}

private val objectMapper = jacksonObjectMapper()

private suspend fun ApplicationCall.respondJson(status: LoginStatus) {
    response.headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    respond(
        status = HttpStatusCode.OK,
        message = objectMapper.writeValueAsString(status)
    )
}

internal data class LoginStatus(
    val authenticated: Boolean,
    val levelOfAssurance: String?
) {
    companion object {
        fun unauthenticated() = LoginStatus(false, null)
        fun authenticated(levelOfAssuranceInternal: IdPortenLevelOfAssurance) = when (levelOfAssuranceInternal) {
            IdPortenLevelOfAssurance.Substantial -> LoginStatus(true, IdPortenLevelOfAssurance.Substantial.name)
            IdPortenLevelOfAssurance.High -> LoginStatus(true, IdPortenLevelOfAssurance.High.name)
            else -> throw IllegalStateException()
        }
    }
}

private fun ApplicationCall.validAccessTokenOrNull(tokenVerifier: TokenVerifier): DecodedJWT? {

    val accessToken = fetchAccessToken(request)

    return if (accessToken != null) {
        try {
            tokenVerifier.verifyAccessToken(accessToken)
        } catch (e: Throwable) {
            null
        }
    } else {
        null
    }
}

private val ApplicationCall.redirectUri: String? get() = request.queryParameters["redirect_uri"]

private val ApplicationCall.levelOfAssurance: String? get() =
    when (val loa = request.queryParameters["loa"] ?: request.queryParameters["level"]) {
        null -> null
        "high", "level4", "Level4" -> "idporten-loa-high"
        "substantial", "level3", "Level3" -> "idporten-loa-substantial"
        else -> loa
    }
