package no.nav.tms.token.support.azure.validation

import io.ktor.server.auth.*
import no.nav.tms.token.support.azure.validation.install.AzureInstaller.performAzureAuthenticatorInstallation


fun AuthenticationConfig.azure(configure: AzureAuthenticatorConfig.() -> Unit = {}) =
    AzureAuthenticatorConfig()
        .also(configure)
        .let { performAzureAuthenticatorInstallation(it) }

// Configuration provided by library user. See readme for example of use
class AzureAuthenticatorConfig {
    var authenticatorName: String = AzureAuthenticator.name
    var setAsDefault: Boolean = false
    var enableDefaultProxy: Boolean = false
}

object AzureAuthenticator {
    const val name = "azure_bearer_access_token"
}

object AzureHeader {
    const val Authorization = "azure-authorization"
}
