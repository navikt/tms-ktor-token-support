package no.nav.tms.token.support.idporten.authentication

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import no.nav.tms.token.support.idporten.authentication.config.Idporten
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(IdTokenAuthenticationProvider::class.java)

// This method configures an authenticator which checks if an end user has hit an authenticated endpoint
// with a valid token cookie. If the user is missing the token cookie, or the provided token is invalid, we redirect
// the user to the endpoint 'oauth2/login', where the user will be prompted to log in through idporten
internal fun Authentication.Configuration.idToken(authenticatorName: String?, configBuilder: () -> AuthConfiguration) {

    val config = configBuilder()
    val provider = IdTokenAuthenticationProvider.build(authenticatorName)

    if (config.shouldRedirect) {
        setupRedirectingInterceptor(provider, config)
    } else {
        setupNonRedirectingInterceptor(provider, config)
    }

    register(provider)
}

private fun setupRedirectingInterceptor(provider: IdTokenAuthenticationProvider, config: AuthConfiguration) {
    val verifier = TokenVerifier(config.jwkProvider, config.clientId, config.issuer)

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val idToken = call.request.cookies[config.tokenCookieName]
        if (idToken != null) {
            try {
                val decodedJWT = verifier.verify(idToken)
                context.principal(IdTokenPrincipal(decodedJWT))
            } catch (e: Throwable) {
                val message = e.message ?: e.javaClass.simpleName
                log.debug("Token verification failed: {}", message)
                call.response.cookies.appendExpired(config.tokenCookieName)
                context.challengeAndRedirect(config.contextPath)
            }
        } else {
            log.debug("Couldn't find cookie ${config.tokenCookieName}.")
            context.challengeAndRedirect(config.contextPath)
        }
    }
}

private fun setupNonRedirectingInterceptor(provider: IdTokenAuthenticationProvider, config: AuthConfiguration) {
    val verifier = createVerifier(config.jwkProvider, config.clientId, config.issuer)

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val idToken = call.request.cookies[config.tokenCookieName]
        if (idToken != null) {
            try {
                val decodedJWT = verifier(idToken).verify(idToken)
                context.principal(IdTokenPrincipal(decodedJWT))
            } catch (e: Throwable) {
                val message = e.message ?: e.javaClass.simpleName
                log.debug("Token verification failed: {}", message)
                call.response.cookies.appendExpired(config.tokenCookieName)
                context.challengeAndRespondUnauthorized()
            }
        } else {
            log.debug("Couldn't find cookie ${config.tokenCookieName}.")
            context.challengeAndRespondUnauthorized()
        }
    }
}

private fun getLoginUrl(contextPath: String): String {
    return if (contextPath.isBlank()) {
        "/oauth2/login"
    } else {
        "/$contextPath/oauth2/login"
    }
}

private fun AuthenticationContext.challengeAndRedirect(contextPath: String) {
    call.response.cookies.append(Idporten.postLoginRedirectCookie, call.request.pathWithParameters(), path = "/$contextPath")

    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) {
        call.respondRedirect(getLoginUrl(contextPath))
        it.complete()
    }
}

private fun AuthenticationContext.challengeAndRespondUnauthorized() {
    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) {
        call.respond(HttpStatusCode.Unauthorized)
        it.complete()
    }
}

private fun ApplicationRequest.pathWithParameters(): String {
    return if (queryParameters.isEmpty()) {
        path()
    } else {
        val params = ParametersBuilder().apply {
            queryParameters.forEach { name, values ->
                appendAll(name, values)
            }
        }.build().formUrlEncode()

        "${path()}?$params"
    }
}

private class IdTokenAuthenticationProvider constructor(config: Configuration) : AuthenticationProvider(config) {

    class Configuration(name: String?) : AuthenticationProvider.Configuration(name)

    companion object {
        fun build(name: String?) = IdTokenAuthenticationProvider(Configuration(name))
    }
}

