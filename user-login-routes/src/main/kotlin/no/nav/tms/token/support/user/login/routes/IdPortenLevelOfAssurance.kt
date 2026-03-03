package no.nav.tms.token.support.user.login.routes

internal enum class IdPortenLevelOfAssurance(
    val acr: String,
    val relativeValue: Int,
    val legacyValue: Int
) {
    Low("idporten-loa-low", 1, -1),
    Substantial("idporten-loa-substantial", 2, 3),
    High("idporten-loa-high", 3, 4);

    companion object {
        fun fromAcr(acr: String): IdPortenLevelOfAssurance {
            return entries
                .find { it.acr.equals(acr, ignoreCase = true) }
                ?: throw IllegalStateException("Could not find matching LoA for claim $acr.")
        }
    }
}
