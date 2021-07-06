package no.nav.tms.token.support.azure.validation

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.azure.validation.config.RuntimeContext
import no.nav.tms.token.support.azure.validation.intercept.azure


fun Application.installAzureAuth(configure: AzureAuthenticatorConfig.() -> Unit = {}) {
    val config = AzureAuthenticatorConfig().also(configure)

    val authenticatorName = getAuthenticatorName(config.setAsDefault)

    val runtimeContext = RuntimeContext()

    install(Authentication) {
        azure(authenticatorName, runtimeContext.verifierWrapper)
    }
}

private fun getAuthenticatorName(isDefault: Boolean): String? {
    return if (isDefault) {
        null
    } else {
        AzureAuthenticator.name
    }
}

// Configuration provided by library user. See readme for example of use
class AzureAuthenticatorConfig {
    var setAsDefault: Boolean = false
}

object AzureAuthenticator {
    const val name = "azure_bearer_access_token"
}
