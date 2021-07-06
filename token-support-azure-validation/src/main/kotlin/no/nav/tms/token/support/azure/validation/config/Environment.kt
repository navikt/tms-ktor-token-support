package no.nav.tms.token.support.azure.validation.config

internal data class Environment(
        val azureClientId: String = getAzureEnvVar("AZURE_APP_CLIENT_ID"),
        val azureWellKnownUrl: String = getAzureEnvVar("AZURE_APP_WELL_KNOWN_URL")
)

private fun getAzureEnvVar(varName: String): String {
    return System.getenv(varName)
            ?: throw IllegalArgumentException("Fant ikke $varName for azure. Påse at nais.yaml er konfigurert riktig.")
}
