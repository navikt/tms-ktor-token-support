package no.nav.tms.token.support.idporten.sidecar.authentication

import kotlinx.serialization.Serializable
import no.nav.tms.token.support.idporten.sidecar.authentication.LevelOfAssuranceInternal.*

@Serializable
internal data class LoginStatus(
    val authenticated: Boolean,
    val level: Int?,
    val levelOfAssurance: String?
) {
    companion object {
        fun unauthenticated() = LoginStatus(false, null, null)
        fun authenticated(levelOfAssuranceInternal: LevelOfAssuranceInternal) = when (levelOfAssuranceInternal) {
            Level3, Substantial -> LoginStatus(true, 3, Substantial.name)
            Level4, High -> LoginStatus(true, 4, High.name)
            else -> throw IllegalStateException()
        }
    }
}
