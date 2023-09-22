package no.nav.tms.token.support.tokenx.validation.install

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import no.nav.tms.token.support.tokenx.validation.TokenXHeader
import no.nav.tms.token.support.tokenx.validation.TokenXPrincipal

internal fun AuthenticationConfig.registerTokenXValidatorProvider(authenticatorName: String?, tokenVerifier: TokenVerifier) {

    AccessTokenAuthenticationProvider.Configuration(authenticatorName)
        .let { config -> AccessTokenAuthenticationProvider(tokenVerifier, config) }
        .let { provider -> register(provider) }
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

private class AccessTokenAuthenticationProvider (val verifier: TokenVerifier, config: Configuration) : AuthenticationProvider(config) {

    private val log = KotlinLogging.logger { }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call

        val accessToken = call.bearerToken
        if (accessToken != null) {
            try {
                val decodedJWT = verifier.verify(accessToken)
                context.principal(TokenXPrincipal(decodedJWT))
            } catch (e: Exception) {
                log.debug(e) { "Token verification failed." }
                context.respondUnauthorized("Invalid or expired token.")
            }
        } else {
            log.debug { "No bearer token found." }
            context.respondUnauthorized("No bearer token found.")
        }
    }

    class Configuration(name: String?) : Config(name)
}
