package no.nav.tms.token.support.idporten.sidecar.mock.authentication

internal data class AuthInfo(
    val alwaysAuthenticated: Boolean,
    val securityLevel: String?,
    val ident: String?,
    val jwtOverride: String?
)
