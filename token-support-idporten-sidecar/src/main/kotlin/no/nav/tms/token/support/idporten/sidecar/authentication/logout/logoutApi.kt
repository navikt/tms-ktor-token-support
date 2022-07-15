package no.nav.tms.token.support.idporten.sidecar.authentication.logout

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.token.support.idporten.sidecar.authentication.config.RuntimeContext

internal fun Routing.idPortenLogoutApi(context: RuntimeContext) {

    get("/logout") {
        call.respondRedirect(getLogoutUrl(context.contextPath))
    }
}

private fun getLogoutUrl(contextPath: String): String {
    return if (contextPath.isBlank()) {
        "/oauth2/logout"
    } else {
        "/$contextPath/oauth2/logout"
    }
}
