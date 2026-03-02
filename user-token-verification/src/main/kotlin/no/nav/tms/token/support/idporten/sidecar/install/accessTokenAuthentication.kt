package no.nav.tms.token.support.idporten.sidecar.install

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import no.nav.tms.token.support.idporten.sidecar.IdPortenTokenPrincipal

private val log = KotlinLogging.logger { }

// This method configures an authenticator which checks if an end user has hit an authenticated endpoint
// with a valid token. If the user is missing the token, or the provided token is invalid, we respond with http-code 401
internal fun AuthenticationConfig.registerIdPortenValidationProvider(authenticatorName: String?, tokenVerifier: TokenVerifier) =
    AccessTokenAuthenticationProvider.Configuration(authenticatorName)
        .let { config -> AccessTokenAuthenticationProvider(tokenVerifier, config) }
        .let { provider -> register(provider) }

private class AccessTokenAuthenticationProvider(
    private val tokenVerifier: TokenVerifier,
    config: Config
) : AuthenticationProvider(config) {

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call

        val accessToken = fetchAccessToken(call.request)

        if (accessToken != null) {
            try {
                val decodedJWT = tokenVerifier.verifyAccessToken(accessToken)
                context.principal(IdPortenTokenPrincipal(decodedJWT))
            } catch (e: Throwable) {
                log.debug(e) { "Token verification failed" }
                context.challengeAndRespondUnauthorized()
            }
        } else {
            log.debug { "Token missing. No header or fallback cookie provided." }
            context.challengeAndRespondUnauthorized()
        }
    }

    class Configuration(name: String?) : Config(name)
}

private fun AuthenticationContext.challengeAndRespondUnauthorized() {
    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
        call.respond(HttpStatusCode.Unauthorized)
        challenge.complete()
    }
}
