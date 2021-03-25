package no.nav.tms.token.support.tokenx.validation.tokendings

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import org.slf4j.LoggerFactory

internal fun Authentication.Configuration.tokenX(authenticatorName: String?, verifier: VerifierWrapper) {

    val provider = AccessTokenAuthenticationProvider.build(authenticatorName)


    val log = LoggerFactory.getLogger(AccessTokenAuthenticationProvider::class.java)

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val accessToken = bearerToken
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
    register(provider)
}

private fun AuthenticationContext.respondUnauthorized(message: String) {

    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) {
        call.respond(HttpStatusCode.Unauthorized, message)
        it.complete()
    }
}

private val bearerRegex = "Bearer .+".toRegex()

private val PipelineContext<*, ApplicationCall>.bearerToken: String? get() {
    return call.request.headers[HttpHeaders.Authorization]
            ?.takeIf { bearerRegex.matches(it) }
            ?.let { it.split(" ")[1] }
}

private class AccessTokenAuthenticationProvider constructor(config: Configuration) : AuthenticationProvider(config) {

    class Configuration(name: String?) : AuthenticationProvider.Configuration(name)

    companion object {
        fun build(name: String?) = AccessTokenAuthenticationProvider(Configuration(name))
    }
}
