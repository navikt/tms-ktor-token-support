package no.nav.tms.token.support.user.token.verification

internal object VerifierInstaller {

    private val tokenxConfig = VerifierConfig(
        wellKnownUrlEnv = "TOKEN_X_WELL_KNOWN_URL",
        audienceEnv = "TOKEN_X_CLIENT_ID"
    )

    private val idPortenConfig = VerifierConfig(
        wellKnownUrlEnv = "IDPORTEN_WELL_KNOWN_URL",
        audienceEnv = "IDPORTEN_AUDIENCE"
    )

    fun installVerifiers(requiredIssuers: List<Issuer>, minLevelOfAssurance: LevelOfAssurance, webProxy: Boolean): Map<String, TokenVerifier> {
        return if (requiredIssuers.isEmpty()) {
            installAllKnownVerifiers(minLevelOfAssurance, webProxy)
        } else {
            installRequiredVerifiersOnly(requiredIssuers, minLevelOfAssurance, webProxy)
        }
    }

    private fun installAllKnownVerifiers(minLevelOfAssurance: LevelOfAssurance, webProxy: Boolean): Map<String, TokenVerifier> {
        if (!tokenxConfig.isPresent() && !idPortenConfig.isPresent()) {
            throw MissingVerifierConfigException("Fant ingen well-known variabler. Påse at nais.yaml er konfigurert riktig")
        }

        val verifiers = mutableMapOf<String, TokenVerifier>()

        if (tokenxConfig.isPresent()) {
            val tokenxVerifier = TokenVerifier.build(
                wellKnownUrl = tokenxConfig.wellKnownUrl,
                audience = tokenxConfig.audience,
                acrMapper = ::mapTokenxAcr,
                minLevelOfAssurance = minLevelOfAssurance,
                webProxy = webProxy
            )

            verifiers[tokenxVerifier.issuer] = tokenxVerifier
        }

        if (idPortenConfig.isPresent()) {
            val idportenVerifier = TokenVerifier.build(
                wellKnownUrl = idPortenConfig.wellKnownUrl,
                audience = idPortenConfig.audience,
                acrMapper = ::mapIdportenAcr,
                minLevelOfAssurance = minLevelOfAssurance,
                webProxy = webProxy
            )

            verifiers[idportenVerifier.issuer] = idportenVerifier
        }

        return verifiers
    }

    private fun installRequiredVerifiersOnly(requiredIssuers: List<Issuer>, minLevelOfAssurance: LevelOfAssurance, webProxy: Boolean): Map<String, TokenVerifier> {

        val verifiers = mutableMapOf<String, TokenVerifier>()

        if (requiredIssuers.contains(Issuer.Tokenx)) {
            if (tokenxConfig.isPresent()) {
                val tokenxVerifier = TokenVerifier.build(
                    wellKnownUrl = tokenxConfig.wellKnownUrl,
                    audience = tokenxConfig.audience,
                    acrMapper = ::mapTokenxAcr,
                    minLevelOfAssurance = minLevelOfAssurance,
                    webProxy = webProxy
                )

                verifiers[tokenxVerifier.issuer] = tokenxVerifier
            } else {
                throw MissingVerifierConfigException("Klarte ikke installere tokenx-verifikator. Mangler env TOKEN_X_WELL_KNOWN_URL")
            }
        }

        if (requiredIssuers.contains(Issuer.IdPorten)) {
            if (idPortenConfig.isPresent()) {
                val idportenVerifier = TokenVerifier.build(
                    wellKnownUrl = idPortenConfig.wellKnownUrl,
                    audience = idPortenConfig.audience,
                    acrMapper = ::mapIdportenAcr,
                    minLevelOfAssurance = minLevelOfAssurance,
                    webProxy = webProxy
                )


                verifiers[idportenVerifier.issuer] = idportenVerifier
            } else {
                throw MissingVerifierConfigException("Klarte ikke installere idporten-verifikator. Mangler env IDPORTEN_WELL_KNOWN_URL")
            }
        }

        return verifiers
    }

    private fun mapTokenxAcr(acr: String): LevelOfAssurance {
        return when(acr) {
            "Level3", "level3", "idporten-loa-substantial" -> LevelOfAssurance.Substantial
            "Level4", "level4", "idporten-loa-high" -> LevelOfAssurance.High
            else -> throw AcrNotSupportedException("Fant ikke loa-mapping for tokenx acr-verdi '$acr'")
        }
    }

    private fun mapIdportenAcr(acr: String): LevelOfAssurance {
        return when(acr) {
            "idporten-loa-low" -> throw AcrNotSupportedException("Støttet ikke idporten level of assurance 'low'")
            "idporten-loa-substantial" -> LevelOfAssurance.Substantial
            "idporten-loa-high" -> LevelOfAssurance.High
            else -> throw AcrNotSupportedException("Fant ikke loa-mapping for idporten acr-verdi '$acr'")
        }
    }

    private class VerifierConfig(
        private val wellKnownUrlEnv: String,
        private val audienceEnv: String
    ) {
        val wellKnownUrl get() = UserTokenVerificationEnvironment.get(wellKnownUrlEnv)!!
        val audience get() = UserTokenVerificationEnvironment.get(audienceEnv)!!

        fun isPresent(): Boolean {
            return UserTokenVerificationEnvironment.get(wellKnownUrlEnv) != null &&
                UserTokenVerificationEnvironment.get(audienceEnv) != null
        }
    }
}

class AcrNotSupportedException(msg: String): IllegalArgumentException(msg)
class MissingVerifierConfigException(msg: String): IllegalStateException(msg)
class VerifierNotFoundException(issuer: String): IllegalArgumentException("Fant ikke verifikator for token med issuer [$issuer]")
