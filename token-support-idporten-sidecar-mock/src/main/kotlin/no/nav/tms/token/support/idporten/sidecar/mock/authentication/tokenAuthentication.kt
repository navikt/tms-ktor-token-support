package no.nav.tms.token.support.idporten.sidecar.mock.authentication

import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

internal fun AuthenticationConfig.idPortenAuthMock(authenticatorName: String?, authInfo: AuthInfo) {

    val provider = AccessTokenAuthenticationProvider.build(authInfo, authenticatorName)

    register(provider)
}

private fun AuthenticationContext.respondUnauthorized(message: String) {

    challenge("Unauthenticated", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
        call.respond(HttpStatusCode.Unauthorized, message)
        challenge.complete()
    }
}

private class AccessTokenAuthenticationProvider constructor(
    val authInfo: AuthInfo,
    config: Configuration
) : AuthenticationProvider(config) {

    private val log = LoggerFactory.getLogger(AccessTokenAuthenticationProvider::class.java)

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        if (authInfo.alwaysAuthenticated) {
            log.debug("Auth is valid as idporten-mock is set to never authorized.")
            context.principal(IdPortenPrincipalBuilder.createPrincipal(authInfo))
        } else {
            log.debug("Responding 401 as idporten-mock is set to never authorized.")
            context.respondUnauthorized("Never authorized.")
        }
    }

    class Configuration(name: String?) : AuthenticationProvider.Config(name)

    companion object {
        fun build(authInfo: AuthInfo, name: String?) = AccessTokenAuthenticationProvider(authInfo, Configuration(name))
    }
}
