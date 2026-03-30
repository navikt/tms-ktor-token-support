package no.nav.tms.token.support.entraid.token.fetcher.impl

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import java.util.concurrent.TimeUnit

internal object CacheBuilder {
    fun buildCache(maxEntries: Long, expiryMarginSeconds: Int): Cache<String, AccessTokenEntry> {
        return Caffeine.newBuilder()
                .expireAfter(createExpiryPolicy(expiryMarginSeconds))
                .maximumSize(maxEntries)
                .build()
    }

    private fun createExpiryPolicy(expiryMarginSeconds: Int) = object : Expiry<String, AccessTokenEntry> {

        override fun expireAfterCreate(key: String, response: AccessTokenEntry, currentTime: Long): Long {
            return TimeUnit.SECONDS.toNanos(response.expiresInSeconds - expiryMarginSeconds)
        }

        override fun expireAfterUpdate(
            key: String,
            value: AccessTokenEntry,
            currentTime: Long,
            currentDuration: Long
        ): Long = currentDuration

        override fun expireAfterRead(
            key: String,
            value: AccessTokenEntry,
            currentTime: Long,
            currentDuration: Long
        ): Long = currentDuration
    }
}

internal data class AccessTokenEntry(
    val accessToken: String,
    val expiresInSeconds: Long
) {
    companion object {
        fun fromResponse(response: TokenResponse) = AccessTokenEntry (
            accessToken = response.accessToken,
            expiresInSeconds = response.expiresIn.toLong()
        )
    }
}
