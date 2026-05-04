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
        cluster = config.clientFilterBuilder?.cluster,
        namespace = config.clientFilterBuilder?.namespace,
        app = config.clientFilterBuilder?.application
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

    internal var clientFilterBuilder: ClientFilterBuilder? = null

    fun filterClients(conf: ClientFilterBuilder.() -> Unit) {
        clientFilterBuilder = ClientFilterBuilder().apply(conf)
    }

    class ClientFilterBuilder internal constructor() {
        var cluster: String? = null
        var namespace: String? = null
        var application: String? = null
    }
}
