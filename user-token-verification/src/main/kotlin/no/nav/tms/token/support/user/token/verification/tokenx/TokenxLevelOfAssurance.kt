package no.nav.tms.token.support.user.token.verification.tokenx

import no.nav.tms.token.support.user.token.verification.LevelOfAssurance
import no.nav.tms.token.support.user.token.verification.idporten.IdPortenLevelOfAssurance

internal enum class TokenxLevelOfAssurance(val acr: String, val relativeValue: Int) {
    Level3("Level3", 2),
    Level4("Level4", 3),
    Low("idporten-loa-low", 1),
    Substantial("idporten-loa-substantial", 2),
    High("idporten-loa-high", 3);

    fun toLevelOfAssurance(): LevelOfAssurance {
        return when (this) {
            High, Level4 -> LevelOfAssurance.High
            Substantial, Level3 -> LevelOfAssurance.Substantial
            Low -> throw IllegalArgumentException("Ingen intern mapping for tokenx loa 'Low'")
        }
    }

    companion object {
        fun fromAcr(acr: String): TokenxLevelOfAssurance {
            return values()
                .find { it.acr.lowercase() == acr.lowercase() }
                ?: throw IllegalStateException("Could not find matching LoA for claim $acr.")
        }

        fun fromLevelOfAssurance(levelOfAssurance: LevelOfAssurance): TokenxLevelOfAssurance {
            return when (levelOfAssurance) {
                LevelOfAssurance.High -> High
                LevelOfAssurance.Substantial -> Substantial
            }
        }
    }
}
