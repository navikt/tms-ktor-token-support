package no.nav.tms.token.support.user.token.verification

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.*
import no.nav.tms.token.support.user.token.verification.idporten.IdPortenTokenVerifier
import kotlin.text.split

private val log = KotlinLogging.logger { }

// This method configures an authenticator which checks if an end user has hit an authenticated endpoint
// with a valid token. If the user is missing the token, or the provided token is invalid, we respond with http-code 401
internal fun AuthenticationConfig.registerIdPortenValidationProvider(authenticatorName: String?, tokenVerifiers: InstalledVerifiers) =
    AccessTokenAuthenticationProvider.Configuration(authenticatorName)
        .let { config -> AccessTokenAuthenticationProvider(tokenVerifiers, config) }
        .let { provider -> register(provider) }

private class AccessTokenAuthenticationProvider(
    private val installedVerifiers: InstalledVerifiers,
    config: Config
) : AuthenticationProvider(config) {

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call

        val accessToken = fetchAccessToken(call.request)

        if (accessToken != null) {
            try {
                val userPrincipal = installedVerifiers.verifyAccessToken(accessToken)
                context.principal(userPrincipal)
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

private val bearerRegex = "Bearer .+".toRegex()

private fun fetchAccessToken(request: ApplicationRequest): DecodedJWT? {
    return request.call
        .request
        .headers[HttpHeaders.Authorization]
        ?.takeIf { bearerRegex.matches(it) }
        ?.let { it.split(" ")[1] }
        ?.let { JWT.decode(it) }
}

private fun AuthenticationContext.challengeAndRespondUnauthorized() {
    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
        call.respond(HttpStatusCode.Unauthorized)
        challenge.complete()
    }
}
