package no.nav.tms.token.support.idporten

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator

object JwkBuilder {
    fun generateJwk(): String {
        return RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("KID")
                .generate()
                .toJSONString()
    }
}
