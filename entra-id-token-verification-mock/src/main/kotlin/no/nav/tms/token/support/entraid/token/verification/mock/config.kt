package no.nav.tms.token.support.entraid.token.verification.mock

import io.ktor.server.auth.*
import no.nav.tms.token.support.entraid.token.verification.NaisApplication


fun AuthenticationConfig.entraIdMock(authenticatorName: String? = null, configure: EntraIdMockedAuthenticatorConfig.() -> Unit = {}) {
    val config = EntraIdMockedAuthenticatorConfig().also(configure)

    registerEntraIdProviderMock(
        authenticatorName = authenticatorName,
        defaultAuthentication = config.getDefaultAuthenticationOrNull()
    )
}

private val defaultIssuedFor =
    NaisApplication("test-cluster", "test-namespace", "test-app_default-provided")

// Configuration provided by library user. See readme for example of use
class EntraIdMockedAuthenticatorConfig {
    internal var defaultAuthenticationConfig: DefaultAuthenticationConfig? = null
    fun enableDefaultAuthentication(config: DefaultAuthenticationConfig.() -> Unit = {}) {
        val configuration = DefaultAuthenticationConfig().apply(config)

        if (configuration.tokenIssuedFor == null) {
            configuration.tokenIssuedFor = defaultIssuedFor
        }

        defaultAuthenticationConfig = configuration
    }

    internal fun getDefaultAuthenticationOrNull(): Authentication?  {
        val config = defaultAuthenticationConfig ?: return null

        return Authentication(
            issuedFor = config.tokenIssuedFor!!,
            userInfo = config.tokenUserInfo,
        )
    }

    class DefaultAuthenticationConfig internal constructor(
        var tokenIssuedFor: NaisApplication? = null,
        var tokenUserInfo: UserInfo? = null
    )
}

internal data class Authentication(
    val issuedFor: NaisApplication,
    val userInfo: UserInfo?
)

data class UserInfo(
    val navIdent: String,
    val userId: String,
    val displayName: String,
    val userName: String,
)
