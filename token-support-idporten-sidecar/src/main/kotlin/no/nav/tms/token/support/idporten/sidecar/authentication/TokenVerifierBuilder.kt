package no.nav.tms.token.support.idporten.sidecar.authentication

import com.auth0.jwk.JwkProvider

// Allow easier mocking of TokenVerifier
internal object TokenVerifierBuilder {
    fun buildTokenVerifier(
        jwkProvider: JwkProvider,
        issuer: String,
        minLevelOfAssurance: LevelOfAssuranceInternal,
    ) = TokenVerifier(
        jwkProvider = jwkProvider,
        issuer = issuer,
        minLevelOfAssurance = minLevelOfAssurance
    )
}
