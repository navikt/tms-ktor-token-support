package no.nav.tms.token.support.tokendings.exchange

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator

internal object JwkBuilder {
    fun generateJwk(): String {
        return RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("KID")
                .generate()
                .toJSONString()
    }
}
