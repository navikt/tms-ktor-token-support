package no.nav.tms.token.support.idporten.sidecar.authentication

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(AccessTokenAuthenticationProvider::class.java)

// This method configures an authenticator which checks if an end user has hit an authenticated endpoint
// with a valid token cookie. If the user is missing the token cookie, or the provided token is invalid, we redirect
// the user to the endpoint 'oauth2/login', where the user will be prompted to log in through idporten
internal fun Authentication.Configuration.idPortenAccessToken(authenticatorName: String?, config: AuthConfiguration) {

    val provider = AccessTokenAuthenticationProvider.build(authenticatorName)

    setupInterceptor(provider, config)

    register(provider)
}

private fun setupInterceptor(provider: AccessTokenAuthenticationProvider, config: AuthConfiguration) {

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->

        val accessToken = fetchAccessToken(call.request, config)

        if (accessToken != null) {
            try {
                val decodedJWT = getVerifiedToken(accessToken, config)
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
}

private fun AuthenticationContext.challengeAndRespondUnauthorized() {
    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) {
        call.respond(HttpStatusCode.Unauthorized)
        it.complete()
    }
}

private fun getVerifiedToken(accessToken: String, config: AuthConfiguration): DecodedJWT {
    val verifier = config.tokenVerifier

    return verifier.verifyAccessToken(accessToken)
}

private class AccessTokenAuthenticationProvider constructor(config: Configuration) : AuthenticationProvider(config) {

    class Configuration(name: String?) : AuthenticationProvider.Configuration(name)

    companion object {
        fun build(name: String?) = AccessTokenAuthenticationProvider(Configuration(name))
    }
}

