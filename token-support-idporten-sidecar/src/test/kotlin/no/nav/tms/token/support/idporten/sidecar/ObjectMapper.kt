package no.nav.tms.token.support.idporten.sidecar

import kotlinx.serialization.json.Json

internal object ObjectMapper {
    val objectMapper = Json {
        ignoreUnknownKeys = true
    }
}
