package no.nav.tms.token.support.idporten.wonderwall.authentication.logout

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.tms.token.support.idporten.wonderwall.authentication.config.RuntimeContext

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
