package no.nav.tms.token.support.idporten.authentication

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import no.nav.tms.token.support.idporten.authentication.config.Idporten
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(IdTokenAuthenticationProvider::class.java)

// This method configures an authenticator which checks if an end user has hit an authenticated endpoint
// with a valid token cookie. If the user is missing the token cookie, or the provided token is invalid, we redirect
// the user to the endpoint 'oauth2/login', where the user will be prompted to log in through idporten
internal fun Authentication.Configuration.idToken(authenticatorName: String, configBuilder: () -> AuthConfiguration) {

    val config = configBuilder()
    val provider = IdTokenAuthenticationProvider.build(authenticatorName)

    val verifier = createVerifier(config.jwkProvider, config.clientId, config.issuer)

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val idToken = call.request.cookies[config.tokenCookieName]
        if (idToken != null) {
            try {
                val decodedJWT = verifier(idToken)?.verify(idToken)
                if (decodedJWT != null) {
                    context.principal(IdTokenPrincipal(decodedJWT))
                } else {
                    call.response.cookies.appendExpired(Idporten.postLoginRedirectCookie)
                    log.debug("Found invalid token: idToken")
                    context.challengeAndRedirect(getLoginUrl(config.contextPath))
                }
            } catch (e: Throwable) {
                val message = e.message ?: e.javaClass.simpleName
                call.response.cookies.appendExpired(Idporten.postLoginRedirectCookie)
                log.debug("Token verification failed: {}", message)
                context.challengeAndRedirect(getLoginUrl(config.contextPath))
            }
        } else {
            log.debug("Couldn't find cookie ${config.tokenCookieName}.")
            call.response.cookies.append(Idporten.postLoginRedirectCookie, call.request.path(), path = config.contextPath)
            context.challengeAndRedirect(getLoginUrl(config.contextPath))
        }
    }
    register(provider)
}

private fun getLoginUrl(contextPath: String): String {
    return if (contextPath.isBlank()) {
        "/oauth2/login"
    } else {
        "/$contextPath/oauth2/login"
    }
}

private fun AuthenticationContext.challengeAndRedirect(loginUrl: String) {
    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) {
        call.respondRedirect(loginUrl)
        it.complete()
    }
}

private class IdTokenAuthenticationProvider constructor(config: Configuration) : AuthenticationProvider(config) {

    class Configuration(name: String) : AuthenticationProvider.Configuration(name)

    companion object {
        fun build(name: String) = IdTokenAuthenticationProvider(Configuration(name))
    }
}
