package no.nav.tms.token.support.idporten.sidecar.authentication

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(AccessTokenAuthenticationProvider::class.java)

// This method configures an authenticator which checks if an end user has hit an authenticated endpoint
// with a valid token. If the user is missing the token, or the provided token is invalid, we respond with http-code 401
internal fun AuthenticationConfig.idPortenAccessToken(authenticatorName: String?, config: AuthConfiguration) {

    val provider = AccessTokenAuthenticationProvider.build(config, authenticatorName)

    register(provider)
}

private class AccessTokenAuthenticationProvider constructor(
    private val authConfig: AuthConfiguration,
    config: Config
) : AuthenticationProvider(config) {

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call

        val accessToken = fetchAccessToken(call.request, authConfig)

        if (accessToken != null) {
            try {
                val decodedJWT = getVerifiedToken(accessToken, authConfig)
                context.principal(IdPortenTokenPrincipal(decodedJWT))
            } catch (e: Throwable) {
                val message = e.message ?: e.javaClass.simpleName
                log.debug("Token verification failed: {}", message)
                context.challengeAndRespondUnauthorized()
            }
        } else {
            log.debug("Token missing. No header or fallback cookie provided.")
            context.challengeAndRespondUnauthorized()
        }
    }

    class Configuration(name: String?) : AuthenticationProvider.Config(name)

    companion object {
        fun build(authConfig: AuthConfiguration, name: String?) = AccessTokenAuthenticationProvider(authConfig, Configuration(name))
    }

}

private fun AuthenticationContext.challengeAndRespondUnauthorized() {
    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
        call.respond(HttpStatusCode.Unauthorized)
        challenge.complete()
    }
}

private fun getVerifiedToken(accessToken: String, config: AuthConfiguration): DecodedJWT {
    val verifier = config.tokenVerifier

    return verifier.verifyAccessToken(accessToken)
}

