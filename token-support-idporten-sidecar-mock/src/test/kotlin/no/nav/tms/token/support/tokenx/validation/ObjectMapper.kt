package no.nav.tms.token.support.idporten.validation

import kotlinx.serialization.json.Json

internal object ObjectMapper {
    val objectMapper = Json {
        ignoreUnknownKeys = true
    }
}
