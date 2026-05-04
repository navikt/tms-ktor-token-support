package no.nav.tms.token.support.user.token.verificaton.mock

import io.ktor.server.auth.*
import no.nav.tms.token.support.user.token.verification.Issuer
import no.nav.tms.token.support.user.token.verification.LevelOfAssurance


fun AuthenticationConfig.userTokenMock(
    configName: String? = null,
    configure: UserTokenMockedAuthenticatorConfig.() -> Unit = {}
) {
    val config = UserTokenMockedAuthenticatorConfig(configName)
        .also(configure)

    registerUserTokenProviderMock(
        authenticatorName = config.configName,
        requiredIssuers = config.issuers.toList(),
        minimumLoa = config.levelOfAssurance,
        defaultAuthentication = config.getDefaultAuthenticationOrNull()
    )
}
// Configuration provided by library user. See readme for example of use
class UserTokenMockedAuthenticatorConfig(
    internal val configName: String?
) {
    internal val issuers = mutableSetOf<Issuer>()
    fun configureIssuers(vararg issuer: Issuer) {
        issuers.addAll(issuer)
    }

    var levelOfAssurance: LevelOfAssurance = LevelOfAssurance.High

    internal var defaultAuthenticationConfig: DefaultAuthenticationConfig? = null
    fun enableDefaultAuthentication(config: DefaultAuthenticationConfig.() -> Unit) {
        val configuration = DefaultAuthenticationConfig().apply(config)

        requireNotNull(configuration.tokenIdent)

        defaultAuthenticationConfig = configuration
    }

    internal fun getDefaultAuthenticationOrNull(): Authentication?  {
        val config = defaultAuthenticationConfig ?: return null

        if (config.tokenIssuer == null) {
            require(issuers.isNotEmpty()) { "Default token issuer må være oppgitt hvis påkrevd issuer ikke er definert" }
            require(issuers.size < 2) { "Default token issuer må være oppgitt hvis flere issuers er påkrevd" }
        } else {
            require(issuers.isEmpty() || issuers.contains(config.tokenIssuer)) {
                "Default token issuer må være en av påkrevde issuers der det er definert"
            }
        }

        require(config.tokenLoa == null || config.tokenLoa!! >= levelOfAssurance) {
            "Default level of assurance må minst være like høy som påkrevd level of assurance"
        }

        return Authentication(
            ident = config.tokenIdent!!,
            issuer = config.tokenIssuer ?: issuers.first(),
            levelOfAssurance = config.tokenLoa ?: levelOfAssurance
        )
    }

    class DefaultAuthenticationConfig internal constructor(
        var tokenIssuer: Issuer? = null,
        var tokenLoa: LevelOfAssurance? = null,
        var tokenIdent: String? = null,
    )
}

internal data class Authentication(
    val issuer: Issuer,
    val levelOfAssurance: LevelOfAssurance,
    val ident: String,
)


