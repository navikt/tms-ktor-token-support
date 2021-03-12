package no.nav.tms.token.support.tokendings.exchange

class TokendingsServiceConfig {
    var cachingEnabled: Boolean = true
    var maxCachedEntries: Long = 1000L
    var cacheExpiryMarginSeconds: Int = 5
}
