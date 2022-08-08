package no.nav.tms.token.support.idporten.sidecar.authentication

import io.ktor.http.*
import io.ktor.server.request.*

internal fun fetchAccessToken(request: ApplicationRequest, config: AuthConfiguration): String? {
    val token = request.bearerToken

    return when {
        token != null -> token
        config.fallbackTokenCookieEnabled -> tokenCookie(request, config.fallbackTokenCookieName)
        else -> null
    }
}

private val bearerRegex = "Bearer .+".toRegex()

private val ApplicationRequest.bearerToken: String? get() {
    return call.request.headers[HttpHeaders.Authorization]
        ?.takeIf { bearerRegex.matches(it) }
        ?.let { it.split(" ")[1] }
}

private fun tokenCookie(request: ApplicationRequest, tokenCookieName: String): String? {
    return request.cookies[tokenCookieName]
}
