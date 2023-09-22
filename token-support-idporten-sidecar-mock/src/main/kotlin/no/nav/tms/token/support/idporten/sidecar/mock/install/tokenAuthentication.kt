package no.nav.tms.token.support.idporten.sidecar.mock.install

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

internal fun AuthenticationConfig.registerIdPortenProviderMock(authenticatorName: String?, authInfo: AuthInfo) {

    AccessTokenAuthenticationProvider.Configuration(authenticatorName)
        .let { config -> AccessTokenAuthenticationProvider(authInfo, config) }
        .let { provider -> register(provider) }
}

private fun AuthenticationContext.respondUnauthorized(message: String) {

    challenge("Unauthenticated", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
        call.respond(HttpStatusCode.Unauthorized, message)
        challenge.complete()
    }
}

private class AccessTokenAuthenticationProvider (
    val authInfo: AuthInfo,
    config: Configuration
) : AuthenticationProvider(config) {

    private val log = KotlinLogging.logger { }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        if (authInfo.alwaysAuthenticated) {
            log.debug { "Auth is valid as idporten-mock is set to never authorized." }
            context.principal(IdPortenPrincipalBuilder.createPrincipal(authInfo))
        } else {
            log.debug { "Responding 401 as idporten-mock is set to never authorized." }
            context.respondUnauthorized("Never authorized.")
        }
    }

    class Configuration(name: String?) : Config(name)
}
