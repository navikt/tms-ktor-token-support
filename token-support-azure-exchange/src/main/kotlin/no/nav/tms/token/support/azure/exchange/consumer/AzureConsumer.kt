package no.nav.tms.token.support.azure.exchange.consumer

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

internal class AzureConsumer(
        private val httpClient: HttpClient,
        private val tentantId: String,
        private val clientId: String,
        azureTokenUrl: String
) {
    private val endpoint = URL(azureTokenUrl)

    suspend fun fetchToken(clientAssertion: String, targetApp: String): AzureTokenResponse {

        return withContext(Dispatchers.IO) {
            val urlParameters = ParametersBuilder().apply {
                append("tenant", tentantId)
                append("client_id", clientId)
                append("scope", "api://$targetApp/.default")
                append("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                append("client_assertion", clientAssertion)
                append("grant_type", "client_credentials")
            }.build()

            httpClient.post {
                url(endpoint)
                body = TextContent(urlParameters.formUrlEncode(), ContentType.Application.FormUrlEncoded)
            }
        }
    }
}
