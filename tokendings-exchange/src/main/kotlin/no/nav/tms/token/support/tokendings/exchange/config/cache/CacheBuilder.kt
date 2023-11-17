package no.nav.tms.token.support.tokendings.exchange.config.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import java.util.concurrent.TimeUnit

internal object CacheBuilder {
    fun buildCache(maxEntries: Long, expiryMarginSeconds: Int): Cache<AccessTokenKey, AccessTokenEntry> {
        return Caffeine.newBuilder()
                .expireAfter(createExpiryPolicy(expiryMarginSeconds))
                .maximumSize(maxEntries)
                .build()
    }

    private fun createExpiryPolicy(expiryMarginSeconds: Int) = object : Expiry<AccessTokenKey, AccessTokenEntry> {
        override fun expireAfterCreate(key: AccessTokenKey, response: AccessTokenEntry, currentTime: Long): Long {
            return TimeUnit.SECONDS.toNanos(response.expiresInSeconds - expiryMarginSeconds)
        }

        override fun expireAfterUpdate(key: AccessTokenKey,
                                       value: AccessTokenEntry,
                                       currentTime: Long,
                                       currentDuration: Long): Long = currentDuration

        override fun expireAfterRead(key: AccessTokenKey,
                                     value: AccessTokenEntry,
                                     currentTime: Long,
                                     currentDuration: Long): Long = currentDuration
    }
}
