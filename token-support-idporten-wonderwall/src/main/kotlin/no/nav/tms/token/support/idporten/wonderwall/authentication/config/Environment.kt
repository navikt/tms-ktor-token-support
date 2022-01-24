package no.nav.tms.token.support.idporten.wonderwall.authentication.config

internal class Environment (
        val idportenWellKnownUrl: String = getIdportenEnvVar("IDPORTEN_WELL_KNOWN_URL"),
        val idportenClientId: String = getIdportenEnvVar("IDPORTEN_CLIENT_ID")
)

private fun getIdportenEnvVar(varName: String): String {
    return System.getenv(varName)
            ?: throw IllegalArgumentException("Fant ikke $varName som brukes i token-support-idporten. Påse at nais.yaml er konfigurert riktig.")
}
