package no.nav.tms.token.support.idporten.validation.mock.tokendings

internal data class AuthInfo(
    val alwaysAuthenticated: Boolean,
    val securityLevel: String?,
    val ident: String?,
    val jwtOverride: String?
)
