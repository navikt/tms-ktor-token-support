package no.nav.tms.token.support.idporten.sidecar.authentication

internal enum class LevelOfAssuranceInternal(val acr: String, val relativeValue: Int) {
    Level3("Level3", 2),
    Level4("Level4", 3),
    Low("idporten-loa-low", 1),
    Substantial("idporten-loa-substantial", 2),
    High("idporten-loa-high", 3);

    companion object {
        fun fromAcr(acr: String): LevelOfAssuranceInternal {
            return values()
                .find { it.acr.lowercase() == acr.lowercase() }
                ?: throw IllegalStateException("Could not find matching LoA for claim $acr.")
        }
    }
}
