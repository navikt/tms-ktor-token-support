package no.nav.tms.token.support.idporten.authentication.refresh

import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.tms.token.support.idporten.authentication.ClientAssertionService
import org.slf4j.LoggerFactory
import java.net.URL

internal class TokenRefreshConsumer(
        private val httpClient: HttpClient,
        private val clientAssertionService: ClientAssertionService,
        private val clientId: String,
        tokenUrlString: String
) {
    private val tokenUrl = URL(tokenUrlString)

    private val log = LoggerFactory.getLogger(TokenRefreshConsumer::class.java)

    suspend fun fetchRefreshedToken(refreshToken: String): String = withContext(Dispatchers.IO) {
        val jwt = clientAssertionService.createClientAssertion()

        val parameters = ParametersBuilder().apply {
            append(OAuth2RequestParameters.ClientId, clientId)
            append(OAuth2RequestParameters.GrantType, "refresh_token")
            append("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
            append("client_assertion", jwt)
            append("refresh_token", refreshToken)
        }.build()

        val response: RefreshTokenResponse = httpClient.post {
            url("$tokenUrl")
            body = FormDataContent(parameters)
        }

        log.info("Response: $response")

        response.refreshToken
    }
}
