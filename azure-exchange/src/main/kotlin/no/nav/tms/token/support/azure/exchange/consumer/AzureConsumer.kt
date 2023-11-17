package no.nav.tms.token.support.azure.exchange.consumer

import io.ktor.client.*
import io.ktor.client.call.*
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
            val urlParameters = listOf (
                "tenant" to tentantId,
                "client_id" to clientId,
                "scope" to "api://$targetApp/.default",
                "client_assertion_type" to "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
                "client_assertion" to clientAssertion,
                "grant_type" to "client_credentials"
            )

            httpClient.post {
                url(endpoint)
                setBody(TextContent(urlParameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
            }.body()
        }
    }
}
