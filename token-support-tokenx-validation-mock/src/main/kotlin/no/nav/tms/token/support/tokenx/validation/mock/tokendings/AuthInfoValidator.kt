package no.nav.tms.token.support.tokenx.validation.mock.tokendings

import com.auth0.jwt.JWT
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel
import no.nav.tms.token.support.tokenx.validation.mock.TokenXAuthenticatorConfig

internal object AuthInfoValidator {

    fun validateAuthInfo(config: TokenXAuthenticatorConfig): AuthInfo {
        return if (config.alwaysAuthenticated) {
            when (config.staticJwtOverride) {
                null -> validateIdentAndSecurityLevel(config)
                else -> validateJwtOverride(config)
            }
        } else {
            AuthInfo(true, null, null, null)
        }
    }

    private fun validateJwtOverride(config: TokenXAuthenticatorConfig): AuthInfo {
        val decodedJWT = JWT.decode(config.staticJwtOverride)

        val claims = decodedJWT.claims

        require(claims.contains("acr_values")) { "jwtOverride må ha claim 'acr_values' med security level" }
        require(claims.contains("pid")) { "jwtOverride må ha claim 'pid' med ident" }

        val securityLevel = claims["acr_values"]!!.asString()
        val ident = claims["pid"]!!.asString()

        val validSecurityLevels = SecurityLevel.values().map { it.claim }

        require(securityLevel in validSecurityLevels) { "'acr_values' må være en av [${validSecurityLevels}]" }

        return AuthInfo(true, securityLevel, ident, config.staticJwtOverride)
    }

    private fun validateIdentAndSecurityLevel(config: TokenXAuthenticatorConfig): AuthInfo {
        require(config.staticUserPid != null) { "Statisk ident må være satt hvis alwaysAuthenticated=true og jwtOverride ikke er satt" }
        require(config.staticSecurityLevel != null) { "Statisk securityLevel må være satt hvis alwaysAuthenticated=true og jwtOverride ikke er satt" }

        return AuthInfo(true, config.staticSecurityLevel?.claim, config.staticUserPid, null)
    }
}
