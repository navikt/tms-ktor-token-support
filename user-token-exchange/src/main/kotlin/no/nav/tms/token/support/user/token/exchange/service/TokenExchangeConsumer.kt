package no.nav.tms.token.support.user.token.exchange.service

import com.fasterxml.jackson.annotation.JsonAlias
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

internal class TokenExchangeConsumer(
    private val httpClient: HttpClient,
    tokenExchangeUrl: String
) {
    private val endpoint = URI.create(tokenExchangeUrl).toURL()

    suspend fun exchangeToken(subjectToken: String, clientAssertion: String, audience: String): TokenExchangeResponse {

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

internal data class TokenExchangeResponse(
    @param:JsonAlias("access_token") val accessToken: String,
    @param:JsonAlias("issued_token_type") val issuedTokenType: String,
    @param:JsonAlias("token_type") val tokenType: String,
    @param:JsonAlias("expires_in") val expiresIn: Int
)

