package no.nav.tms.token.support.idporten

internal class Environment (
        val idportenWellKnownUrl: String = getIdportenEnvVar("IDPORTEN_WELL_KNOWN_URL"),
        val idportenClientId: String = getIdportenEnvVar("IDPORTEN_CLIENT_ID"),
        val idportenClientJwk: String = getIdportenEnvVar("IDPORTEN_CLIENT_JWK"),
        val idportenRedirectUri: String = getIdportenEnvVar("IDPORTEN_REDIRECT_URI"),
)

private fun getIdportenEnvVar(varName: String): String {
    return System.getenv(varName)
            ?: throw IllegalArgumentException("Fant ikke $varName som brukes i token-support-idporten. Påse at nais.yaml er konfigurert riktig.")
}

private fun getEnvVar(varName: String, default: String = ""): String {
    return System.getenv(varName)
            ?: default
}
