package no.nav.tms.token.support.entraid.token.verification.mock

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import no.nav.tms.token.support.entraid.token.verification.NaisApplication

private val objectMapper = jacksonObjectMapper()

fun HttpRequestBuilder.mockUnauthorizedHeader() {
    headers[HttpHeaders.Authorization] = MockUnauthorizedHeader
}

private val defaultIssuedFor =
    NaisApplication("test-cluster", "test-namespace", "test-app_header-provided")

fun HttpRequestBuilder.mockAuthorizedHeader(
    issuedFor: NaisApplication = defaultIssuedFor,
    userInfo: UserInfo? = null
) {
    val defaultAuthentication = Authentication(
        issuedFor = issuedFor,
        userInfo = userInfo
    )

    headers[HttpHeaders.Authorization] = "$MockAuthorizedHeader ${objectMapper.writeValueAsString(defaultAuthentication)}"
}
