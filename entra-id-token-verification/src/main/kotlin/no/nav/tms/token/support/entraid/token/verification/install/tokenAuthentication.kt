package no.nav.tms.token.support.entraid.token.verification.install

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

internal fun AuthenticationConfig.registerEntraIdVerificationProvider(authenticatorName: String?, tokenVerifier: TokenVerifier) {

    AccessTokenAuthenticationProvider.Configuration(authenticatorName)
        .let { config -> AccessTokenAuthenticationProvider(tokenVerifier, config) }
        .let { provider -> register(provider) }
}

private class AccessTokenAuthenticationProvider(
    val verifier: TokenVerifier,
    config: Config
) : AuthenticationProvider(config) {

    val log = KotlinLogging.logger { }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val accessToken = context.call.bearerToken
        if (accessToken != null) {
            try {
                val entraIdPrincipal = verifier.verify(accessToken)
                context.principal(entraIdPrincipal)
            } catch (e: TokenVerifier.EntraIdAccessException) {
                log.debug(e) { "Tokenverifisering feilet: ${e.description}" }
                context.respondUnauthorized("Ugyldig eller utgått token.")
            } catch (e: Exception) {
                log.debug(e) { "Tokenverifisering feilet" }
                context.respondUnauthorized("Ugyldig eller utgått token.")
            }
        } else {
            log.debug { "Fant ikke bearer-token." }
            context.respondUnauthorized("Fant ikke bearer-token.")
        }
    }

    class Configuration(name: String?) : Config(name)
}

private fun AuthenticationContext.respondUnauthorized(message: String) {
    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
        call.respond(HttpStatusCode.Unauthorized, message)
        challenge.complete()
    }
}

private val bearerRegex = "Bearer .+".toRegex()

private val ApplicationCall.bearerToken: String? get() {
    return request.headers[HttpHeaders.Authorization]
        ?.takeIf { bearerRegex.matches(it) }
        ?.let { it.split(" ")[1] }
}
