package no.nav.tms.token.support.user.token.exchange.service

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

internal data class AccessTokenEntry(
    val accessToken: String,
    val expiresInSeconds: Long
) {
    companion object {
        fun fromResponse(response: TokenExchangeResponse) = AccessTokenEntry (
            accessToken = response.accessToken,
            expiresInSeconds = response.expiresIn.toLong()
        )
    }
}

internal data class AccessTokenKey(
    val subject: String,
    val securityLevel: String,
    val targetApp: String
)
