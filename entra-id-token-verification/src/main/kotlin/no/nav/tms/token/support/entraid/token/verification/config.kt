package no.nav.tms.token.support.entraid.token.verification

import io.ktor.server.auth.*
import no.nav.tms.token.support.entraid.token.verification.install.AccessFilter
import no.nav.tms.token.support.entraid.token.verification.install.EntraIdVerifierBuilder
import no.nav.tms.token.support.entraid.token.verification.install.registerEntraIdVerificationProvider


fun AuthenticationConfig.entraId(
    authenticatorName: String? = null,
    configure: AzureAuthenticatorConfig.() -> Unit = {}
) {
    val config = AzureAuthenticatorConfig()
        .also(configure)

    val accessFilter = AccessFilter(
        allowUserAccess = config.allowUserAccess,
        cluster = config.filterClientCluster,
        namespace = config.filterClientNamespace,
        app = config.filterClientAppName
    )

    registerEntraIdVerificationProvider(
        authenticatorName = authenticatorName,
        tokenVerifier = EntraIdVerifierBuilder.buildTokenVerifier(
            accessFilter = accessFilter,
            enableDefaultProxy = config.enableDefaultProxy
        )
    )
}

// Configuration provided by library user. See readme for example of use
class AzureAuthenticatorConfig {
    var enableDefaultProxy: Boolean = false

    var allowUserAccess: Boolean = true
    var filterClientCluster: String? = null
    var filterClientNamespace: String? = null
    var filterClientAppName: String? = null
}
