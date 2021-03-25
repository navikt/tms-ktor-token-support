package no.nav.tms.token.support.tokenx.validation.config

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import java.net.URL
import java.util.concurrent.TimeUnit

internal object JwkProviderBuilder {
    fun createJwkProvider(metadata: OauthServerConfigurationMetadata): JwkProvider = JwkProviderBuilder(URL(metadata.jwksUri))
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
}
