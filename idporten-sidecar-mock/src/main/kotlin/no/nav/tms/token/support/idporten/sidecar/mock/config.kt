package no.nav.tms.token.support.idporten.sidecar.mock

import io.ktor.server.auth.*
import no.nav.tms.token.support.idporten.sidecar.IdPortenAuthenticator
import no.nav.tms.token.support.idporten.sidecar.mock.install.IdPortenMockInstaller.performIdPortenMockInstallation


fun AuthenticationConfig.idPortenMock(configure: IdPortenMockedAuthenticatorConfig.() -> Unit = {}) {
    val config = IdPortenMockedAuthenticatorConfig().also(configure)

    performIdPortenMockInstallation(config)
}

enum class LevelOfAssurance(val claim: String) {
    SUBSTANTIAL("idporten-loa-substantial"),
    HIGH("idporten-loa-high")
}

// Configuration provided by library user. See readme for example of use
class IdPortenMockedAuthenticatorConfig {
    var authenticatorName: String = IdPortenAuthenticator.name
    var setAsDefault: Boolean = false
    var alwaysAuthenticated: Boolean = false
    var staticLevelOfAssurance: LevelOfAssurance? = null
    var staticUserPid: String? = null
    var staticJwtOverride: String? = null
}
