package no.nav.tms.token.support.user.token.verificaton.mock

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import no.nav.tms.token.support.user.token.verification.Issuer
import no.nav.tms.token.support.user.token.verification.LevelOfAssurance

private val objectMapper = jacksonObjectMapper()

fun HttpRequestBuilder.mockUnauthorizedHeader() {
    headers[HttpHeaders.Authorization] = MockUnauthorizedHeader
}

fun HttpRequestBuilder.mockAuthorizedHeader(
    ident: String,
    levelOfAssurance: LevelOfAssurance,
    issuer: Issuer
) {
    val defaultAuthentication = Authentication(
        ident = ident,
        levelOfAssurance = levelOfAssurance,
        issuer = issuer,
    )

    headers[HttpHeaders.Authorization] = "$MockAuthorizedHeader ${objectMapper.writeValueAsString(defaultAuthentication)}"
}
