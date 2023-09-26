package no.nav.tms.token.support.tokenx.validation.mock

import io.ktor.server.auth.*
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.mock.install.TokenXMockInstaller.performTokenXMockInstallation


fun AuthenticationConfig.tokenXMock(configure: TokenXMockedAuthenticatorConfig.() -> Unit = {}) {
    val config = TokenXMockedAuthenticatorConfig().also(configure)

    performTokenXMockInstallation(config)
}

enum class LevelOfAssurance(val claim: String) {
    LEVEL_3("Level3"),
    LEVEL_4("Level4"),
    SUBSTANTIAL("idporten-loa-substantial"),
    HIGH("idporten-loa-high")
}

// Configuration provided by library user. See readme for example of use
class TokenXMockedAuthenticatorConfig {
    var authenticatorName: String = TokenXAuthenticator.name
    var setAsDefault: Boolean = false
    var alwaysAuthenticated: Boolean = false
    var staticLevelOfAssurance: LevelOfAssurance? = null
    var staticUserPid: String? = null
    var staticJwtOverride: String? = null
}
