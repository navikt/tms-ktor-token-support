package no.nav.tms.token.support.azure.exchange.config

import no.nav.tms.token.support.azure.exchange.AzureEnvironment

internal class Environment (
        val azureClientId: String = getAzureEnvVar("AZURE_APP_CLIENT_ID"),
        val azureTenantId: String = getAzureEnvVar("AZURE_APP_TENANT_ID"),
        val azureJwk: String = getAzureEnvVar("AZURE_APP_JWK"),
        val azureOpenidIssuer: String = getAzureEnvVar("AZURE_OPENID_CONFIG_ISSUER"),
        val azureOpenidTokenEndpoint: String = getAzureEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")
)

private fun getAzureEnvVar(varName: String) = AzureEnvironment.get(varName)
    ?: throw IllegalArgumentException("Fant ikke $varName for azure. Påse at nais.yaml er konfigurert riktig.")
