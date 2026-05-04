package no.nav.tms.token.support.user.token.exchange

interface UserTokenExchanger {
    suspend fun exchangeToken(token: String, targetApp: String): String
}

class UserTokenExchangeException(cause: Exception, target: String):
    RuntimeException("Feil ved veksling av token for å nå app [$target]", cause)


