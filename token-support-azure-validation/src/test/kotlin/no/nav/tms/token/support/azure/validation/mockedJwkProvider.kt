package no.nav.tms.token.support.azure.validation

import com.auth0.jwk.Jwk
import com.auth0.jwk.SigningKeyNotFoundException
import com.nimbusds.jose.jwk.RSAKey

internal fun createMockedJwkProvider(publicJwk: RSAKey) = { kid: String ->
    if (publicJwk.keyID == kid) {
        publicJwk.toJSONObject()
                .run { toMap() }
                .let { Jwk.fromValues(it) }
    } else {
        throw SigningKeyNotFoundException("", Exception())
    }
}
