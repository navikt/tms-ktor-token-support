package no.nav.tms.token.support.idporten.sidecar.install

import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.date.*
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUserFactory

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
            call.response.cookies.append(postLoginRedirectCookie, redirectUri)
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
            call.respondJson(LoginStatus.authenticated(IdportenUserFactory.extractLevelOfAssurance(idToken)))
        }
    }

    get("/login/callback") {
        call.response.cookies.append(
            name = postLoginRedirectCookie,
            value = "",
            expires = GMTDate.START
        )

        call.request.cookies[postLoginRedirectCookie]
            ?.let { call.respondRedirect(it) }
            ?: call.respond(HttpStatusCode.OK, "Login successful")
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
    val level: Int?,
    val levelOfAssurance: String?
) {
    companion object {
        fun unauthenticated() = LoginStatus(false, null, null)
        fun authenticated(levelOfAssuranceInternal: IdPortenLevelOfAssurance) = when (levelOfAssuranceInternal) {
            IdPortenLevelOfAssurance.Level3, IdPortenLevelOfAssurance.Substantial -> LoginStatus(true, 3, IdPortenLevelOfAssurance.Substantial.name)
            IdPortenLevelOfAssurance.Level4, IdPortenLevelOfAssurance.High -> LoginStatus(true, 4, IdPortenLevelOfAssurance.High.name)
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
