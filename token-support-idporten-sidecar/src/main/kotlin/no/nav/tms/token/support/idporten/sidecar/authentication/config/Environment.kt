package no.nav.tms.token.support.idporten.sidecar.authentication.config

internal class Environment (
        val idportenWellKnownUrl: String = getIdportenEnvVar("IDPORTEN_WELL_KNOWN_URL")
)

private fun getIdportenEnvVar(varName: String): String {
    return System.getenv(varName)
            ?: throw IllegalArgumentException("Fant ikke $varName som brukes i token-support-idporten. Påse at nais.yaml er konfigurert riktig.")
}
