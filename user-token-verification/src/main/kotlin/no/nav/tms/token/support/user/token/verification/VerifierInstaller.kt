package no.nav.tms.token.support.user.token.verification

import com.auth0.jwt.interfaces.DecodedJWT
import no.nav.tms.token.support.user.token.verification.idporten.IdPortenVerifierBuilder
import no.nav.tms.token.support.user.token.verification.tokenx.TokenxVerifierBuilder

internal interface TokenVerifier {
    fun verify(accessToken: DecodedJWT): UserPrincipal
}

internal class InstalledVerifiers(
    private val verifierByIssuer: Map<String, TokenVerifier>
) {
    fun verifyAccessToken(accessToken: DecodedJWT): UserPrincipal {
        return verifierByIssuer[accessToken.issuer]?.let { verifier ->
            verifier.verify(accessToken)
        }?: throw VerifierNotFoundException(accessToken.issuer)
    }
}

internal class VerifierInstaller(
    enableProxy: Boolean
) {
    private val tokenxVerifierBuilder: TokenxVerifierBuilder?
    private val idPortenVerifierBuilder: IdPortenVerifierBuilder?

    init {
        val tokenxWellKnown = UserTokenVerificationEnvironment.get("TOKEN_X_WELL_KNOWN_URL")

        tokenxVerifierBuilder = if (tokenxWellKnown != null) {
            TokenxVerifierBuilder(tokenxWellKnown, enableProxy)
        } else {
            null
        }

        val idportenWellknown = UserTokenVerificationEnvironment.get("IDPORTEN_WELL_KNOWN_URL")

        idPortenVerifierBuilder = if (idportenWellknown != null) {
            IdPortenVerifierBuilder(idportenWellknown, enableProxy)
        } else {
            null
        }
    }

    fun installVerifiers(requiredIssuers: List<Issuer>, minLevelOfAssurance: LevelOfAssurance): InstalledVerifiers {
        return if (requiredIssuers.isEmpty()) {
            installAllKnownVerifiers(minLevelOfAssurance)
        } else {
            installRequiredVerifiersOnly(requiredIssuers, minLevelOfAssurance)
        }
    }

    private fun installAllKnownVerifiers(minLevelOfAssurance: LevelOfAssurance): InstalledVerifiers {
        if (tokenxVerifierBuilder == null && idPortenVerifierBuilder == null) {
            throw MissingVerifierConfigException("Fant ingen well-known variabler. Påse at nais.yaml er konfigurert riktig")
        }

        val verifiers = mutableMapOf<String, TokenVerifier>()

        if (tokenxVerifierBuilder != null) {
            val tokenxVerifier = tokenxVerifierBuilder.buildTokenVerifier(minLevelOfAssurance)

            verifiers[tokenxVerifier.issuer] = tokenxVerifier
        }

        if (idPortenVerifierBuilder != null) {
            val idportenVerifier = idPortenVerifierBuilder.buildTokenVerifier(minLevelOfAssurance)

            verifiers[idportenVerifier.issuer] = idportenVerifier
        }

        return InstalledVerifiers(verifiers)
    }

    private fun installRequiredVerifiersOnly(requiredIssuers: List<Issuer>, minLevelOfAssurance: LevelOfAssurance): InstalledVerifiers {

        val verifiers = mutableMapOf<String, TokenVerifier>()

        if (requiredIssuers.contains(Issuer.Tokenx)) {
            if (tokenxVerifierBuilder == null) {
                throw MissingVerifierConfigException("Klarte ikke installere tokenx-verifikator. Mangler env TOKEN_X_WELL_KNOWN_URL")
            } else {
                val tokenxVerifier = tokenxVerifierBuilder.buildTokenVerifier(minLevelOfAssurance)

                verifiers[tokenxVerifier.issuer] = tokenxVerifier
            }
        }

        if (requiredIssuers.contains(Issuer.IdPorten)) {
            if (idPortenVerifierBuilder == null) {
                throw MissingVerifierConfigException("Klarte ikke installere idporten-verifikator. Mangler env IDPORTEN_WELL_KNOWN_URL")
            } else {
                val idportenVerifier = idPortenVerifierBuilder.buildTokenVerifier(minLevelOfAssurance)

                verifiers[idportenVerifier.issuer] = idportenVerifier
            }
        }

        return InstalledVerifiers(verifiers)
    }
}

class MissingVerifierConfigException(msg: String): IllegalStateException(msg)
class VerifierNotFoundException(issuer: String): IllegalArgumentException("Fant ikke verifikator for token med issuer [$issuer]")
