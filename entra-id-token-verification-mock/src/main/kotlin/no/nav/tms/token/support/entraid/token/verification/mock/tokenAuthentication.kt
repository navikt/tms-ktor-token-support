package no.nav.tms.token.support.entraid.token.verification.mock

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationProvider

import io.ktor.server.response.*

internal const val MockAuthorizedHeader = "MockAuthorized"
internal const val MockUnauthorizedHeader = "MockUnauthorized"

internal fun AuthenticationConfig.registerEntraIdProviderMock(
    authenticatorName: String?,
    defaultAuthentication: Authentication?
) {
    AccessTokenAuthenticationProvider.Configuration(authenticatorName)
        .let { config -> AccessTokenAuthenticationProvider(defaultAuthentication, config) }
        .let { provider -> register(provider) }
}

private fun AuthenticationContext.respondUnauthorized(message: String) {

    challenge("Unauthenticated", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
        call.respond(HttpStatusCode.Unauthorized, message)
        challenge.complete()
    }
}

private class AccessTokenAuthenticationProvider(
    val defaultAuthentication: Authentication?,
    config: Configuration
) : AuthenticationProvider(config) {

    private val log = KotlinLogging.logger { }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val authHeader = context.mockAuthHeader()

        if (authHeader == null) {
            if (defaultAuthentication != null) {
                log.debug { "Call is authorized due to default authentication config." }
                context.principal(EntraIdPrincipalBuilder.createPrincipal(defaultAuthentication))
            } else {
                log.debug { "Call is unauthorized as no default authentication or header was provided" }
                context.respondUnauthorized("Not authorized.")
            }
        } else if (authHeader.authorized) {
            log.debug { "Call is authorized as MockAuthorized header is provided" }
            context.principal(EntraIdPrincipalBuilder.createPrincipal(authHeader.authentication!!))
        } else {
            log.debug { "Call is unauthorized as MockUnauthorized header is provided" }
            context.respondUnauthorized("Not authorized due to header.")
        }
    }

    class Configuration(name: String?) : Config(name)

    private val unauthorizedPattern = MockUnauthorizedHeader.toRegex()
    private val authorizedPattern = "$MockAuthorizedHeader (.*)".toRegex()

    private val objectMapper = jacksonObjectMapper()

    private fun parseTokenContent(header: String): Authentication {
        return authorizedPattern.matchEntire(header)
            ?.destructured
            ?.let { (match) ->
                objectMapper.readValue(match)
            } ?: throw IllegalStateException("Klarte ikke lese tokenInfo fra auth-header")
    }

    private fun AuthenticationContext.mockAuthHeader(): MockAuth? {
        val authHeader = call.request.headers[HttpHeaders.Authorization]

        return if (authHeader == null) {
            null
        } else if (unauthorizedPattern.matches(authHeader)) {
            MockAuth(false, null)
        } else if (authorizedPattern.matches(authHeader)) {
            MockAuth(true, parseTokenContent(authHeader))
        } else null
    }

    private class MockAuth(
        val authorized: Boolean,
        val authentication: Authentication?
    )
}
