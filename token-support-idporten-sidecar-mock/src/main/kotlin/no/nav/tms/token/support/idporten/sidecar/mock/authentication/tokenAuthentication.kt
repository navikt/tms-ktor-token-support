package no.nav.tms.token.support.idporten.sidecar.mock.authentication

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import org.slf4j.LoggerFactory

internal fun Authentication.Configuration.idportenAuthMock(authenticatorName: String?, authInfo: AuthInfo) {

    val provider = AccessTokenAuthenticationProvider.build(authenticatorName)

    val log = LoggerFactory.getLogger(AccessTokenAuthenticationProvider::class.java)

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        if (authInfo.alwaysAuthenticated) {
            log.debug("Auth is valid as idporten-mock is set to never authorized.")
            context.principal(IdPortenPrincipalBuilder.createPrincipal(authInfo))
        } else {
            log.debug("Responding 401 as idporten-mock is set to never authorized.")
            context.respondUnauthorized("Never authorized.")
        }
    }
    register(provider)
}

private fun AuthenticationContext.respondUnauthorized(message: String) {

    challenge("Unauthenticated", AuthenticationFailedCause.InvalidCredentials) {
        call.respond(HttpStatusCode.Unauthorized, message)
        it.complete()
    }
}

private class AccessTokenAuthenticationProvider constructor(config: Configuration) : AuthenticationProvider(config) {

    class Configuration(name: String?) : AuthenticationProvider.Configuration(name)

    companion object {
        fun build(name: String?) = AccessTokenAuthenticationProvider(Configuration(name))
    }
}
