package no.nav.tms.token.support.tokenx.validation.tokendings

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import no.nav.tms.token.support.tokenx.validation.tokendings.Verifier.createVerifier
import org.slf4j.LoggerFactory

internal fun Authentication.Configuration.tokenDings(authenticatorName: String?, configBuilder: () -> TokenDingsConfig) {

    val config = configBuilder()

    val provider = AccessTokenAuthenticationProvider.build(authenticatorName)

    val verifier = createVerifier(config.jwkProvider, config.clientId, config.issuer)

    val log = LoggerFactory.getLogger(AccessTokenAuthenticationProvider::class.java)

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val accessToken = bearerToken
        if (accessToken != null) {
            try {
                val decodedJWT = verifier(accessToken)?.verify(accessToken)
                if (decodedJWT != null) {
                    context.principal(TokenDingsPrincipal(decodedJWT))
                } else {
                    log.debug("Found invalid token: accessToken")
                    call.respond(HttpStatusCode.Unauthorized)
                }
            } catch (e: Throwable) {
                val message = e.message ?: e.javaClass.simpleName
                log.debug("Token verification failed: {}", message)
                call.respond(HttpStatusCode.Unauthorized)
            }
        } else {
            log.debug("No bearer token found.")
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
    register(provider)
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
