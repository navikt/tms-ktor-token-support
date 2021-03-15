package no.nav.tms.token.support.tokendings.exchange.config

internal class Environment (
        val tokenxWellKnownUrl: String = getTokenxEnvVar("TOKEN_X_WELL_KNOWN_URL"),
        val tokenxClientId: String = getTokenxEnvVar("TOKEN_X_CLIENT_ID"),
        val tokenxClientJwk: String = getTokenxEnvVar("TOKEN_X_PRIVATE_JWK")
)

private fun getTokenxEnvVar(varName: String): String {
    return System.getenv(varName)
            ?: throw IllegalArgumentException("Fant ikke $varName for tokenx. Påse at nais.yaml er konfigurert riktig.")
}
