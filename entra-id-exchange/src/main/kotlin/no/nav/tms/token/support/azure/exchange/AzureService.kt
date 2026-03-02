package no.nav.tms.token.support.azure.exchange

interface AzureService {
    suspend fun getAccessToken(targetApp: String): String
}
