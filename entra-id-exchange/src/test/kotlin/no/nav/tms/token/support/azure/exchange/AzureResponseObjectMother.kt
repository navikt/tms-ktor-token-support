package no.nav.tms.token.support.azure.exchange

import no.nav.tms.token.support.azure.exchange.consumer.AzureTokenResponse


internal object AzureResponseObjectMother {
    fun createAzureResponse(accessToken: String, expiresIn: Int = 300) = AzureTokenResponse (
            accessToken = accessToken,
            tokenType = "",
            expiresIn = expiresIn,
            extExpiresIn = expiresIn
    )
}
