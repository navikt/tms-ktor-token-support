package no.nav.tms.token.support.entraid.token.fetcher.impl

internal class EntraIdEnvironment (
    val clientId: String = getEntraIdEnvVar("AZURE_APP_CLIENT_ID"),
    val tenantId: String = getEntraIdEnvVar("AZURE_APP_TENANT_ID"),
    val privateJwk: String = getEntraIdEnvVar("AZURE_APP_JWK"),
    val openidIssuer: String = getEntraIdEnvVar("AZURE_OPENID_CONFIG_ISSUER"),
    val openidTokenEndpoint: String = getEntraIdEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")
)

private fun getEntraIdEnvVar(varName: String) = EntraIdEnvironment.get(varName)
    ?: throw IllegalArgumentException("Fant ikke $varName for entra-id/azure. Påse at nais.yaml er konfigurert riktig.")
