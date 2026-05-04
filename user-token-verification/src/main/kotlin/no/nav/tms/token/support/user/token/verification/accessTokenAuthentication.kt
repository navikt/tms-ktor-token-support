package no.nav.tms.token.support.user.token.verification

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.*
import kotlin.text.split

private val log = KotlinLogging.logger { }

// This method configures an authenticator which checks if an end user has hit an authenticated endpoint
// with a valid token. If the user is missing the token, or the provided token is invalid, we respond with http-code 401
internal fun AuthenticationConfig.registerUserTokenAuthenticator(authenticatorName: String?, tokenVerifiers: Map<String, TokenVerifier>) =
    AccessTokenAuthenticationProvider.Configuration(authenticatorName)
        .let { config -> AccessTokenAuthenticationProvider(tokenVerifiers, config) }
        .let { provider -> register(provider) }

private class AccessTokenAuthenticationProvider(
    private val installedVerifiers: Map<String, TokenVerifier>,
    config: Config
) : AuthenticationProvider(config) {

    override suspend fun onAuthenticate(context: AuthenticationContext) {

        val accessToken = fetchAccessToken(context.call.request)

        if (accessToken == null) {
            log.debug { "Mangler token - Authorization header er tom, eller ikke på bearer-format" }
            context.challengeAndRespondUnauthorized()
            return
        }

        val verifier = installedVerifiers[accessToken.issuer]

        if (verifier == null) {
            log.debug { "Fant ikke installert verifikator for issuer [${accessToken.issuer}]" }
            context.challengeAndRespondUnauthorized()
            return
        }

        try {
            context.principal(verifier.verify(accessToken))
        } catch (e: Throwable) {
            log.debug(e) { "Verifisering av token feilet." }
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
        ?.let {
            JWT.decode(it)
        }
}

private fun AuthenticationContext.challengeAndRespondUnauthorized() {
    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
        call.respond(HttpStatusCode.Unauthorized)
        challenge.complete()
    }
}
