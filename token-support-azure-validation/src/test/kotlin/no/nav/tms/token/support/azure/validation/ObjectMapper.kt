package no.nav.tms.token.support.azure.validation

import kotlinx.serialization.json.Json

internal object ObjectMapper {
    val objectMapper = Json {
        ignoreUnknownKeys = true
    }
}
