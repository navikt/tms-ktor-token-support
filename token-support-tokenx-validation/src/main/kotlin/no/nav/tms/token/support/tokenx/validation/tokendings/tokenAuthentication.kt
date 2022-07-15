package no.nav.tms.token.support.tokenx.validation.tokendings

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import no.nav.tms.token.support.tokenx.validation.TokenXHeader
import org.slf4j.LoggerFactory

internal fun AuthenticationConfig.tokenXAccessToken(authenticatorName: String?, verifier: TokenVerifier) {

    val provider = AccessTokenAuthenticationProvider.build(verifier, authenticatorName)

    register(provider)
}

private fun AuthenticationContext.respondUnauthorized(message: String) {

    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
        call.respond(HttpStatusCode.Unauthorized, message)
        challenge.complete()
    }
}

private val bearerRegex = "Bearer .+".toRegex()

private val ApplicationCall.bearerToken: String? get() {
    return tokenFromTokenxHeader()
        ?: tokenFromAuthHeader()
}

private fun ApplicationCall.tokenFromTokenxHeader(): String? {
    return request.headers[TokenXHeader.Authorization]
        ?.takeIf { bearerRegex.matches(it) }
        ?.let { it.split(" ")[1] }
}

private fun ApplicationCall.tokenFromAuthHeader(): String? {
    return request.headers[HttpHeaders.Authorization]
        ?.takeIf { bearerRegex.matches(it) }
        ?.let { it.split(" ")[1] }
}


private class AccessTokenAuthenticationProvider constructor(val verifier: TokenVerifier, config: Configuration) : AuthenticationProvider(config) {

    private val log = LoggerFactory.getLogger(AccessTokenAuthenticationProvider::class.java)

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call

        val accessToken = call.bearerToken
        if (accessToken != null) {
            try {
                val decodedJWT = verifier.verify(accessToken)
                context.principal(TokenXPrincipal(decodedJWT))
            } catch (e: Exception) {
                val message = e.message ?: e.javaClass.simpleName
                log.debug("Token verification failed: {}", message)
                context.respondUnauthorized("Invalid or expired token.")
            }
        } else {
            log.debug("No bearer token found.")
            context.respondUnauthorized("No bearer token found.")
        }
    }

    class Configuration(name: String?) : AuthenticationProvider.Config(name)

    companion object {
        fun build(verifier: TokenVerifier, name: String?) = AccessTokenAuthenticationProvider(verifier, Configuration(name))
    }
}
