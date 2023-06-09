package no.nav.tms.token.support.tokenx.validation

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance.SUBSTANTIAL
import no.nav.tms.token.support.tokenx.validation.TokenXInstaller.performTokenXAuthenticatorInstallation
import no.nav.tms.token.support.tokenx.validation.config.RuntimeContext
import no.nav.tms.token.support.tokenx.validation.tokendings.tokenXAccessToken


fun Application.installTokenXAuth(configure: TokenXAuthenticatorConfig.() -> Unit = {}) {
    val config = TokenXAuthenticatorConfig().also(configure)

    performTokenXAuthenticatorInstallation(config)
}

// Configuration provided by library user. See readme for example of use
class TokenXAuthenticatorConfig {
    var setAsDefault: Boolean = false
    var levelOfAssurance: LevelOfAssurance = SUBSTANTIAL
}

object TokenXAuthenticator {
    const val name = "tokenx_bearer_access_token"
}

object TokenXHeader {
    const val Authorization = "token-x-authorization"
}

enum class LevelOfAssurance {
    SUBSTANTIAL, // Equivalent to old Level3
    HIGH // Equivalent to old Level4
}
