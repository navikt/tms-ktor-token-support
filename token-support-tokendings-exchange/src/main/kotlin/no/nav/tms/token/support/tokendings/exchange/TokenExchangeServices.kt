package no.nav.tms.token.support.tokendings.exchange

import no.nav.tms.token.support.tokendings.exchange.config.TokenDingsContext

object TokenExchangeServices {
    private val context = TokenDingsContext()

    val tokenDingsService: TokenDingsService get() = context.tokenDingsService
    val targetAppNameBuilder: TargetAppNameBuilder get() = context.targetAppNameBuilder
}
