package no.nav.tms.token.support.tokenx.validation.config

internal data class Environment(
        val tokenxClientId: String = getTokenxEnvVar("TOKEN_X_CLIENT_ID"),
        val tokenxWellKnownUrl: String = getTokenxEnvVar("TOKEN_X_WELL_KNOWN_URL")
)

private fun getTokenxEnvVar(varName: String): String {
    return System.getenv(varName)
            ?: throw IllegalArgumentException("Fant ikke $varName for tokenx. Påse at nais.yaml er konfigurert riktig.")
}
