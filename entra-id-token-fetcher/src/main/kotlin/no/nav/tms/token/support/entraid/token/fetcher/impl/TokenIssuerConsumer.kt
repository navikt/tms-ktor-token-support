package no.nav.tms.token.support.entraid.token.fetcher.impl

import com.fasterxml.jackson.annotation.JsonAlias
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

internal class TokenIssuerConsumer(
    private val httpClient: HttpClient,
    private val tentantId: String,
    private val clientId: String,
    tokenIssueUrl: String
) {
    private val endpoint = URI.create(tokenIssueUrl).toURL()

    suspend fun fetchToken(clientAssertion: String, targetApp: String): TokenResponse {

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

internal data class TokenResponse(
    @param:JsonAlias("access_token") val accessToken: String,
    @param:JsonAlias("expires_in") val expiresIn: Int,
)
