package no.nav.tms.token.support.tokendings.exchange

import no.nav.tms.token.support.tokendings.exchange.config.TokendingsContext

object TokenExchangeServices {
    private val context = TokendingsContext()

    val tokendingsService: TokendingsService get() = context.tokendingsService
    val targetAppNameBuilder: TargetAppNameBuilder get() = context.targetAppNameBuilder
}
