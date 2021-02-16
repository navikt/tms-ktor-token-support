package no.nav.tms.token.support.idporten

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.routing.*

fun Application.installIdPortenAuth(configure: IdportenAuthenticationConfig.() -> Unit) {
    val config = IdportenAuthenticationConfig().apply(configure)
    val contextPath = environment.rootPath
    val cookieName = config.tokenCookieName

    require(cookieName.isNotBlank()) { "Navn på token-cookie må spesifiseres." }

    val runtimeContext = RuntimeContext(
            tokenCookieName = cookieName,
            contextPath = contextPath,
            postLoginRedirectUri = config.postLoginRedirectUri
    )

    install(Authentication) {
        oauth(Idporten.authenticatorName) {
            client = HttpClient(CIO)
            providerLookup = { runtimeContext.oauth2ServerSettings }
            urlProvider = { runtimeContext.environment.idportenRedirectUri }
        }



        idToken(IdPortenCookieAuthenticator.name) {
            AuthConfiguration (
                    jwkProvider = runtimeContext.jwkProvider,
                    contextPath = contextPath,
                    tokenCookieName = cookieName,
                    clientId = runtimeContext.environment.idportenClientId,
                    issuer = runtimeContext.metadata.issuer
            )
        }
    }

    routing {
        loginApi(runtimeContext)
    }

}

class IdportenAuthenticationConfig (
    var postLoginRedirectUri: String = "",
    var tokenCookieName: String = ""
)

object IdPortenCookieAuthenticator {
    const val name = "idporten_cookie"
}
