package no.nav.tms.token.support.idporten.sidecar

import kotlinx.serialization.json.Json

internal object ObjectMapper {
    val kotlinxMapper = Json {
        ignoreUnknownKeys = true
    }
}
