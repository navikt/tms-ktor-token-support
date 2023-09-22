package no.nav.tms.token.support.idporten.sidecar.install

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUserFactory

private const val postLoginRedirectCookie = "redirect_uri"

internal fun Routing.idPortenLoginApi(tokenVerifier: TokenVerifier, routesPrefix: String?) {

    if (routesPrefix != null) {
        route("/$routesPrefix") {
            loginEndPoints(tokenVerifier, routesPrefix)
        }
    } else {
        loginEndPoints(tokenVerifier, routesPrefix)
    }
}

private fun Route.loginEndPoints(tokenVerifier: TokenVerifier, routesPrefix: String?) {
    get("/login") {
        val redirectUri = call.redirectUri

        if (redirectUri != null) {
            call.response.cookies.append(postLoginRedirectCookie, redirectUri)
        }

        call.respondRedirect(getLoginUrl(routesPrefix, call.levelOfAssurance))
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

private val objectMapper = Json

private suspend fun ApplicationCall.respondJson(status: LoginStatus) {
    response.headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    respond(
        status = HttpStatusCode.OK,
        message = objectMapper.encodeToString(status)
    )
}

@Serializable
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

private fun getLoginUrl(contextPath: String?, levelOfAssurance: String?): String {
    val redirectPath = if (contextPath != null) {
        "/$contextPath/oauth2/login?redirect=/$contextPath/login/callback"
    } else {
        "/oauth2/login?redirect=/login/callback"
    }

    return if (levelOfAssurance != null) {
        "$redirectPath&level=$levelOfAssurance"
    } else {
        redirectPath
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
