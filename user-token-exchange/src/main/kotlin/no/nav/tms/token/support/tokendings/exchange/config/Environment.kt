package no.nav.tms.token.support.tokendings.exchange.config

import no.nav.tms.token.support.tokendings.exchange.TokenXEnvironment

internal class Environment (
        val tokenxWellKnownUrl: String = getTokenxEnvVar("TOKEN_X_WELL_KNOWN_URL"),
        val tokenxClientId: String = getTokenxEnvVar("TOKEN_X_CLIENT_ID"),
        val tokenxClientJwk: String = getTokenxEnvVar("TOKEN_X_PRIVATE_JWK")
)

private fun getTokenxEnvVar(varName: String) = TokenXEnvironment.get(varName)
    ?: throw IllegalArgumentException("Fant ikke $varName for tokenx. Påse at nais.yaml er konfigurert riktig.")
