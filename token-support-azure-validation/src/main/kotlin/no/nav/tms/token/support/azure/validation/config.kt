package no.nav.tms.token.support.azure.validation

import io.ktor.application.*
import no.nav.tms.token.support.azure.validation.AzureInstaller.performAzureAuthenticatorInstallation


fun Application.installAzureAuth(configure: AzureAuthenticatorConfig.() -> Unit = {}) {
    val config = AzureAuthenticatorConfig().also(configure)

    performAzureAuthenticatorInstallation(config)
}

// Configuration provided by library user. See readme for example of use
class AzureAuthenticatorConfig {
    var setAsDefault: Boolean = false
    var enableDefaultProxy: Boolean = false
}

object AzureAuthenticator {
    const val name = "azure_bearer_access_token"
}

object AzureHeader {
    const val Authorization = "azure-authorization"
}
