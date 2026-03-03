package no.nav.tms.token.support.user.login.routes

import io.ktor.http.*
import io.ktor.server.request.*

internal fun fetchAccessToken(request: ApplicationRequest) =
    request.call
        .request
        .headers[HttpHeaders.Authorization]
        ?.takeIf { bearerRegex.matches(it) }
        ?.let { it.split(" ")[1] }

private val bearerRegex = "Bearer .+".toRegex()
