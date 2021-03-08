package no.nav.tms.token.support.tokendings.exchange.consumer

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

internal class TokendingsConsumer(
        private val httpClient: HttpClient,
        tokendingsUrl: String
) {
    private val endpoint = URL(tokendingsUrl)

    suspend fun exchangeToken(subjectToken: String, clientAssertion: String, audience: String): TokendingsTokenResponse {

        return withContext(Dispatchers.IO) {
            val urlParameters = ParametersBuilder().apply {
                append("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                append("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                append("client_assertion", clientAssertion)
                append("subject_token_type", "urn:ietf:params:oauth:token-type:jwt")
                append("subject_token", subjectToken)
                append("audience", audience)
            }.build()

            httpClient.post {
                url(endpoint)
                body = TextContent(urlParameters.formUrlEncode(), ContentType.Application.FormUrlEncoded)
            }
        }
    }
}
