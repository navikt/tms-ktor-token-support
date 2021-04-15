package no.nav.tms.token.support.idporten.authentication

import kotlinx.serialization.Serializable

@Serializable
internal data class LoginStatus(
    val authenticated: Boolean,
    val level: Int?
) {
    companion object {
        fun unAuthenticated() = LoginStatus(false, null)
        fun authenticated(level: Int) = LoginStatus(true, level)
    }
}
