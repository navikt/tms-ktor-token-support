package no.nav.tms.token.support.tokendings.exchange.consumer

import io.ktor.client.*
import io.ktor.client.call.*
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
            val urlParameters = listOf(
                "grant_type" to "urn:ietf:params:oauth:grant-type:token-exchange",
                "client_assertion_type" to "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
                "client_assertion" to clientAssertion,
                "subject_token_type" to "urn:ietf:params:oauth:token-type:jwt",
                "subject_token" to subjectToken,
                "audience" to audience
            )

            httpClient.post {
                url(endpoint)
                setBody(TextContent(urlParameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
            }.body()
        }
    }
}
