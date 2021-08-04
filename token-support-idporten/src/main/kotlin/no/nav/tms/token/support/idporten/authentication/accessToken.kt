package no.nav.tms.token.support.idporten.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import no.nav.tms.token.support.idporten.authentication.config.Idporten
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(AccessTokenAuthenticationProvider::class.java)

// This method configures an authenticator which checks if an end user has hit an authenticated endpoint
// with a valid token cookie. If the user is missing the token cookie, or the provided token is invalid, we redirect
// the user to the endpoint 'oauth2/login', where the user will be prompted to log in through idporten
internal fun Authentication.Configuration.idPortenAccessToken(authenticatorName: String?, configBuilder: () -> AuthConfiguration) {

    val config = configBuilder()
    val provider = AccessTokenAuthenticationProvider.build(authenticatorName)

    if (config.shouldRedirect) {
        setupRedirectingInterceptor(provider, config)
    } else {
        setupNonRedirectingInterceptor(provider, config)
    }

    register(provider)
}

private fun setupRedirectingInterceptor(provider: AccessTokenAuthenticationProvider, config: AuthConfiguration) {

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->

        if (call.request.hasRequiredCookies(config)) {
            try {
                val decodedJWT = call.getVerifiedToken(config)

                context.principal(IdPortenTokenPrincipal(decodedJWT))
            } catch (e: Throwable) {
                val message = e.message ?: e.javaClass.simpleName
                log.debug("Token verification failed: {}", message)
                context.challengeAndRedirect(config.contextPath)
            }
        } else {
            log.debug("Couldn't find cookie ${config.accessTokenCookieName}.")
            context.challengeAndRedirect(config.contextPath)
        }
    }
}

private fun setupNonRedirectingInterceptor(provider: AccessTokenAuthenticationProvider, config: AuthConfiguration) {

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->

        if (call.request.hasRequiredCookies(config)) {
            try {
                val decodedJWT = call.getVerifiedToken(config)
                context.principal(IdPortenTokenPrincipal(decodedJWT))
            } catch (e: Throwable) {
                val message = e.message ?: e.javaClass.simpleName
                log.debug("Token verification failed: {}", message)
                context.challengeAndRespondUnauthorized()
            }
        } else {
            log.debug("Couldn't find cookies ${config.accessTokenCookieName} or ${config.refreshTokenCookieName}.")
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

private fun ApplicationRequest.hasRequiredCookies(config: AuthConfiguration): Boolean {
    return cookies[config.accessTokenCookieName] != null &&
        cookies[config.refreshTokenCookieName] != null
}

private suspend fun ApplicationCall.getVerifiedToken(config: AuthConfiguration): DecodedJWT {
    val verifier = TokenVerifier(config.jwkProvider, config.clientId, config.issuer)

    val accessToken = request.cookies[config.accessTokenCookieName]!!
    val refreshToken = request.cookies[config.refreshTokenCookieName]!!

    val currentToken = verifier.verifyAccessToken(accessToken)

    return if (config.shouldRefreshToken && config.tokenRefreshService.shouldRefreshToken(currentToken)) {
        val refreshedToken = refreshAccessTokenCookie(refreshToken, config)

        if (refreshedToken != null) {
            refreshedToken
        } else {
            currentToken
        }
    } else {
        currentToken
    }
}

private suspend fun ApplicationCall.refreshAccessTokenCookie(refreshToken: String, config: AuthConfiguration): DecodedJWT? {
    return try {
        val result = config.tokenRefreshService.getRefreshedToken(refreshToken)

        setAccessTokenCookie(result.accessToken, config)
        setRefreshTokenCookie(result.refreshToken, config)

        JWT.decode(result.accessToken)
    } catch (e: Exception) {
        log.warn("Was unable to refresh access token.")
        null
    }
}

private fun ApplicationCall.setAccessTokenCookie(accessToken: String, config: AuthConfiguration) {
    response.cookies.append(
            name = config.accessTokenCookieName,
            value = accessToken,
            secure = config.secureCookie,
            httpOnly = true,
            path = "/${config.contextPath}"
    )
}

private fun ApplicationCall.setRefreshTokenCookie(refreshToken: String, config: AuthConfiguration) {
    response.cookies.append(
            name = config.refreshTokenCookieName,
            value = refreshToken,
            secure = config.secureCookie,
            httpOnly = true,
            path = "/${config.contextPath}"
    )
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

private class AccessTokenAuthenticationProvider constructor(config: Configuration) : AuthenticationProvider(config) {

    class Configuration(name: String?) : AuthenticationProvider.Configuration(name)

    companion object {
        fun build(name: String?) = AccessTokenAuthenticationProvider(Configuration(name))
    }
}

