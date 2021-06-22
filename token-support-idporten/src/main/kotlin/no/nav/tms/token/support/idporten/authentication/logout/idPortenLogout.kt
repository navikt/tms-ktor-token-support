package no.nav.tms.token.support.idporten.authentication.logout

import com.auth0.jwt.JWT
import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.idporten.authentication.LogoutPrincipal
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(IdTokenAuthenticationProvider::class.java)

internal fun Authentication.Configuration.idPortenLogout(authenticatorName: String?, configBuilder: () -> LogoutConfig) {

    val config = configBuilder()

    val provider = IdTokenAuthenticationProvider.build(authenticatorName)

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->

        val idToken = call.request.cookies[config.idTokenCookieName]

        if (idToken != null) {
            try {
                val decodedIdToken = JWT.decode(idToken)

                context.principal(LogoutPrincipal(decodedIdToken))
            } catch (e: Throwable) {
                val message = e.message ?: e.javaClass.simpleName
                log.debug("Token verification failed during logout: {}", message)
            }
        } else {
            log.debug("Couldn't find cookie with name ${config.idTokenCookieName} during logout.")
        }
    }
    register(provider)
}

internal class LogoutConfig(
        val idTokenCookieName: String
)

private class IdTokenAuthenticationProvider constructor(config: Configuration) : AuthenticationProvider(config) {

    class Configuration(name: String?) : AuthenticationProvider.Configuration(name)

    companion object {
        fun build(name: String?) = IdTokenAuthenticationProvider(Configuration(name))
    }
}
