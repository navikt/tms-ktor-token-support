package no.nav.tms.token.support.user.token.verificaton.mock

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import no.nav.tms.token.support.user.token.verification.Issuer
import no.nav.tms.token.support.user.token.verification.LevelOfAssurance

internal const val MockAuthorizedHeader = "MockAuthorized"
internal const val MockUnauthorizedHeader = "MockUnauthorized"

internal const val IdPortenMockIssuer = "http://idporten-mock"
internal const val TokenxMockIssuer = "http://tokenx-mock"

internal const val MockLoaSubstantial = "test-loa-substantial"
internal const val MockLoaHigh = "test-loa-high"

internal fun AuthenticationConfig.registerUserTokenProviderMock(
    authenticatorName: String?,
    requiredIssuers: List<Issuer>,
    minimumLoa: LevelOfAssurance,
    defaultAuthentication: Authentication?
) {
    AccessTokenAuthenticationProvider.Configuration(authenticatorName)
        .let { config -> AccessTokenAuthenticationProvider(defaultAuthentication, requiredIssuers, minimumLoa, config) }
        .let { provider -> register(provider) }
}

private class AccessTokenAuthenticationProvider (
    private val defaultAuthentication: Authentication?,
    private val requiredIssuers: List<Issuer>,
    private val minimumLoa: LevelOfAssurance,
    config: Configuration
) : AuthenticationProvider(config) {

    class Configuration(name: String?) : Config(name)

    private val log = KotlinLogging.logger { }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val authHeader = context.mockAuthHeader()

        if (authHeader == null) {
            if (defaultAuthentication != null) {
                log.debug { "Call is authorized due to default authentication config." }
                context.principal(UserTokenPrincipalBuilder.createPrincipal(defaultAuthentication))
            } else {
                log.debug { "Call is unauthorized as no default authentication or header was provided" }
                context.respondUnauthorized("Not authorized.")
            }
        } else if (authHeader.authorized) {
            if (isValid(authHeader.authentication!!, requiredIssuers, minimumLoa)) {
                log.debug { "Call is authorized as MockAuthorized header is provided with valid contents" }
                context.principal(UserTokenPrincipalBuilder.createPrincipal(authHeader.authentication))
            } else {
                log.debug { "Call is unauthorized as contents of MockAuthorized were invalid with respect to config" }
                context.respondUnauthorized("Not authorized due to header.")
            }
        } else {
            log.debug { "Call is unauthorized as MockUnauthorized header is provided" }
            context.respondUnauthorized("Not authorized due to header.")
        }
    }

    private fun isValid(authInfo: Authentication, requiredIssuers: List<Issuer>, minimumLoa: LevelOfAssurance): Boolean {
        return authInfo.levelOfAssurance >= minimumLoa &&
            (requiredIssuers.isEmpty() || requiredIssuers.contains(authInfo.issuer))
    }

    private fun AuthenticationContext.respondUnauthorized(message: String) {

        challenge("Unauthenticated", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
            call.respond(HttpStatusCode.Unauthorized, message)
            challenge.complete()
        }
    }

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
