package no.nav.tms.token.support.idporten.sidecar.install

internal enum class IdPortenLevelOfAssurance(val acr: String, val relativeValue: Int) {
    Low("idporten-loa-low", 1),
    Substantial("idporten-loa-substantial", 2),
    High("idporten-loa-high", 3);

    companion object {
        fun fromAcr(acr: String): IdPortenLevelOfAssurance {
            return values()
                .find { it.acr.lowercase() == acr.lowercase() }
                ?: throw IllegalStateException("Could not find matching LoA for claim $acr.")
        }
    }
}
