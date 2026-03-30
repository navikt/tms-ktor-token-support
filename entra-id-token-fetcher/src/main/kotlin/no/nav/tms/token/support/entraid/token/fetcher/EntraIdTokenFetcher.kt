package no.nav.tms.token.support.entraid.token.fetcher

interface EntraIdTokenFetcher {
    suspend fun getAccessToken(targetApp: String): String
}
