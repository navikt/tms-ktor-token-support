package no.nav.tms.token.support.idporten

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import org.slf4j.LoggerFactory

internal class IdTokenAuthenticationProvider constructor(config: Configuration) : AuthenticationProvider(config) {

    internal class Configuration(name: String) : AuthenticationProvider.Configuration(name)

    companion object {
        fun build(name: String) = IdTokenAuthenticationProvider(Configuration(name))
    }
}

class AuthConfiguration(
        val contextPath: String,
        val tokenCookieName: String,
        val jwkProvider: JwkProvider,
        val clientId: String,
        val issuer: String
)

private val log = LoggerFactory.getLogger(IdTokenAuthenticationProvider::class.java)

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
                    log.info("Found invalid token: idToken")
                }
            } catch (e: Throwable) {
                val message = e.message ?: e.javaClass.simpleName
                log.error("Token verification failed: {}", message)
            }
        } else {
            log.info("Couldn't find token.")
            call.response.cookies.append(Idporten.postLoginRedirectCookie, call.request.path(), path = config.contextPath)
            context.challengeAndRedirect(getLoginUrl(config.contextPath))
        }
    }
    register(provider)
}

fun createVerifier(jwkProvider: JwkProvider, clientId: String, issuer: String): (String) -> JWTVerifier? = {
    jwkProvider.get(JWT.decode(it).keyId).idTokenVerifier(
            clientId,
            issuer
    )
}

private fun getLoginUrl(contextPath: String): String {
    return if (contextPath.isBlank()) {
        "oauth2/login"
    } else {
        "$contextPath/oauth2/login"
    }
}

private fun AuthenticationContext.challengeAndRedirect(loginUrl: String) {
    challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) {
        call.respondRedirect(loginUrl)
        it.complete()
    }
}

internal data class IdTokenPrincipal(val decodedJWT: DecodedJWT) : Principal
