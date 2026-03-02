package no.nav.tms.token.support.azure.validation.install

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import no.nav.tms.token.support.azure.validation.AzureHeader
import no.nav.tms.token.support.azure.validation.AzurePrincipal

internal fun AuthenticationConfig.registerAzureValidationProvider(authenticatorName: String?, tokenVerifier: TokenVerifier) {

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
                val decodedJWT = verifier.verify(accessToken)
                context.principal(AzurePrincipal(decodedJWT))
            } catch (e: Exception) {
                log.debug(e) { "Token verification failed" }
                context.respondUnauthorized("Invalid or expired token.")
            }
        } else {
            log.debug { "No bearer token found." }
            context.respondUnauthorized("No bearer token found.")
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
    return tokenFromAzureHeader()
        ?: tokenFromAuthHeader()
}

private fun ApplicationCall.tokenFromAzureHeader(): String? {
    return request.headers[AzureHeader.Authorization]
        ?.takeIf { bearerRegex.matches(it) }
        ?.let { it.split(" ")[1] }
}

private fun ApplicationCall.tokenFromAuthHeader(): String? {
    return request.headers[HttpHeaders.Authorization]
        ?.takeIf { bearerRegex.matches(it) }
        ?.let { it.split(" ")[1] }
}
