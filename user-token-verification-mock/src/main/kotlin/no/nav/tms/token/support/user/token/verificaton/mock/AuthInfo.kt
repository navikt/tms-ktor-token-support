package no.nav.tms.token.support.user.token.verificaton.mock

import no.nav.tms.token.support.user.token.verification.Issuer
import no.nav.tms.token.support.user.token.verification.LevelOfAssurance

internal data class AuthInfo(
    val authenticatedByDefault: Boolean,
    val defaultDefaultAuthentication: Authentication?
)

internal data class Authentication(
    val issuer: Issuer,
    val levelOfAssurance: LevelOfAssurance,
    val ident: String,
)
