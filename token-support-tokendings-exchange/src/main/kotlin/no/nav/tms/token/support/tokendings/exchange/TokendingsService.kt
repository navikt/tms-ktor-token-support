package no.nav.tms.token.support.tokendings.exchange

interface TokendingsService {
    suspend fun exchangeToken(token: String, targetApp: String): String
}
