package no.nav.tms.token.support.tokenx.validation.mock.tokendings

import com.auth0.jwt.JWT
import no.nav.tms.token.support.tokenx.validation.mock.LevelOfAssurance
import no.nav.tms.token.support.tokenx.validation.mock.TokenXMockedAuthenticatorConfig

internal object AuthInfoValidator {

    fun validateAuthInfo(config: TokenXMockedAuthenticatorConfig): AuthInfo {
        return if (config.alwaysAuthenticated) {
            when (config.staticJwtOverride) {
                null -> validateIdentAndSecurityLevel(config)
                else -> validateJwtOverride(config)
            }
        } else {
            AuthInfo(false, null, null, null)
        }
    }

    private fun validateJwtOverride(config: TokenXMockedAuthenticatorConfig): AuthInfo {
        val decodedJWT = JWT.decode(config.staticJwtOverride)

        val claims = decodedJWT.claims

        require(claims.contains("acr")) { "jwtOverride må ha claim 'acr' med security level" }
        require(claims.contains("pid")) { "jwtOverride må ha claim 'pid' med ident" }

        val securityLevel = claims["acr"]!!.asString()
        val ident = claims["pid"]!!.asString()

        val validLevels = LevelOfAssurance.values().map { it.claim }

        require(securityLevel in validLevels) { "'acr' må være en av [${validLevels}]" }

        return AuthInfo(true, securityLevel, ident, config.staticJwtOverride)
    }

    private fun validateIdentAndSecurityLevel(config: TokenXMockedAuthenticatorConfig): AuthInfo {
        require(config.staticUserPid != null) { "Statisk ident må være satt hvis alwaysAuthenticated=true og jwtOverride ikke er satt" }
        require(config.staticLevelOfAssurance != null) { "Statisk securityLevel må være satt hvis alwaysAuthenticated=true og jwtOverride ikke er satt" }

        return AuthInfo(true, config.staticLevelOfAssurance?.claim, config.staticUserPid, null)
    }
}
