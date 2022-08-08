package no.nav.tms.token.support.azure.validation.intercept

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import no.nav.tms.token.support.azure.validation.AzureHeader
import no.nav.tms.token.support.azure.validation.AzurePrincipal
import org.slf4j.LoggerFactory

internal fun AuthenticationConfig.azureAccessToken(authenticatorName: String?, verifier: TokenVerifier) {

    val provider = AccessTokenAuthenticationProvider.build(verifier, authenticatorName)

    register(provider)
}

private class AccessTokenAuthenticationProvider constructor(
    val verifier: TokenVerifier,
    config: Config
) : AuthenticationProvider(config) {

    val log = LoggerFactory.getLogger(AccessTokenAuthenticationProvider::class.java)

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val accessToken = context.call.bearerToken
        if (accessToken != null) {
            try {
                val decodedJWT = verifier.verify(accessToken)
                context.principal(AzurePrincipal(decodedJWT))
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
