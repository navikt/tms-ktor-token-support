package no.nav.tms.token.support.entraid.token.validation.mock.install

internal data class AuthInfo(
    val alwaysAuthenticated: Boolean,
    val azureJwt: String?
)
