package no.nav.tms.token.support.tokenx.validation.mock.tokendings

internal data class AuthInfo(
    val alwaysAuthenticated: Boolean,
    val securityLevel: String?,
    val ident: String?,
    val jwtOverride: String?
)
