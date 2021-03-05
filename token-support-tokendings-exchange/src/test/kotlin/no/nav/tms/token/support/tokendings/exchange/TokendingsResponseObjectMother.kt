package no.nav.tms.token.support.tokendings.exchange

import no.nav.tms.token.support.tokendings.exchange.consumer.TokendingsTokenResponse

internal object TokendingsResponseObjectMother {
    fun createTokendingsResponse(accessToken: String) = TokendingsTokenResponse (
            accessToken = accessToken,
            tokenType = "",
            issuedTokenType = "",
            expiresIn = "300"
    )
}
