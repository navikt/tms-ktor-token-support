package no.nav.tms.token.support.idporten.authentication.config

internal object Idporten {
    const val scope = "openid"
    const val idTokenParameter = "id_token"
    const val authenticatorName = "tms_token_support_idporten"
    const val postLoginRedirectCookie = "redirect_uri"
}
