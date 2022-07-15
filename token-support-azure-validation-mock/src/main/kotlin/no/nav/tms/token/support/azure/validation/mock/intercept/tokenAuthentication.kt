package no.nav.tms.token.support.azure.validation.mock.intercept

import com.auth0.jwt.JWT
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import no.nav.tms.token.support.azure.validation.AzurePrincipal
import org.slf4j.LoggerFactory

internal fun AuthenticationConfig.azureAuthMock(authenticatorName: String?, authInfo: AuthInfo) {

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
            log.debug("Auth is valid as azure-mock is set to never authorized.")
            context.principal(AzurePrincipalBuilder.createPrincipal(authInfo))
        } else {
            log.debug("Responding 401 as azure-mock is set to never authorized.")
            context.respondUnauthorized("Never authorized.")
        }
    }

    class Configuration(name: String?) : AuthenticationProvider.Config(name)

    companion object {
        fun build(authInfo: AuthInfo, name: String?) = AccessTokenAuthenticationProvider(authInfo, Configuration(name))
    }
}
