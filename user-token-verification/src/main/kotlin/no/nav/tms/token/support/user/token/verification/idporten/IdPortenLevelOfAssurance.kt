package no.nav.tms.token.support.user.token.verification.idporten

import no.nav.tms.token.support.user.token.verification.LevelOfAssurance

internal enum class IdPortenLevelOfAssurance(val acr: String, val relativeValue: Int) {
    Low("idporten-loa-low", 1),
    Substantial("idporten-loa-substantial", 2),
    High("idporten-loa-high", 3);

    fun toLevelOfAssurance(): LevelOfAssurance {
        return when (this) {
            High -> LevelOfAssurance.High
            Substantial -> LevelOfAssurance.Substantial
            Low -> throw IllegalArgumentException("Ingen intern mapping for idporten loa 'Low'")
        }
    }

    companion object {
        fun fromAcr(acr: String): IdPortenLevelOfAssurance {
            return values()
                .find { it.acr.lowercase() == acr.lowercase() }
                ?: throw IllegalStateException("Could not find matching LoA for claim $acr.")
        }

        fun fromLevelOfAssurance(levelOfAssurance: LevelOfAssurance): IdPortenLevelOfAssurance {
            return when (levelOfAssurance) {
                LevelOfAssurance.High -> High
                LevelOfAssurance.Substantial -> Substantial
            }
        }
    }
}
