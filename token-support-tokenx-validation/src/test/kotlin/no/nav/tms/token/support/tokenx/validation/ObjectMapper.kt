package no.nav.tms.token.support.tokenx.validation

import kotlinx.serialization.json.Json

internal object ObjectMapper {
    val kotlinxMapper = Json {
        ignoreUnknownKeys = true
    }
}
