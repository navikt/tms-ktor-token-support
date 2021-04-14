# token-support-idporten

Dette biblioteket tilbyr en måte for en ktor app å autentisere en bruker mot idporten.

## Oppsett

Bruk av biblioteket forutsetter at nais-yaml er konfigurert for idporten:

```yaml
spec:
  idporten:
    enabled: true
```

For å kunne autentisere et endepunkt må man først installere autentikatoren.

Her er det 5 variabler:

`tokenCookieName`: (Required) Navn på token-cookien som settes i browser etter bruker har logget inn.
`postLogoutRedirectUri`: (Required) Bestemmer hvor bruker sendes etter de har logget ut. Denne må samsvare med nais-yaml.
`postLoginRedirectUri`: (Optional) Url der bruker havner etter login dersom vi ikke finner en "redirect_uri" cookie. Default ""
`setAsDefault`: (Optional) Setter denne autentikatoren som default. Default 'false'
`secureCookie`: (Optional) Setter token-cookie som secure, slik at den kun sendes med https-kall. Default 'true'
 
Eksempel på konfigurasjon:

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        tokenCookieName = "user_id_token"
        postLogoutRedirectUri = "https://www.nav.no"
        postLoginRedirectUri = '/post/login'
        setAsDefault = false
        secureCookie = true
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Det er viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        tokenCookieName = "user_id_token"
        postLogoutRedirectUri = "https://www.nav.no"
        postLoginRedirectUri = '/post/login'
        setAsDefault = false
        secureCookie = true
    }
    
    routing {
        authenticate(IdPortenCookieAuthenticator.name) {
            get("/sikret") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
```

Typisk eksempel på bruk i miljø og dette er default authenticator:

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        tokenCookieName = "user_id_token"
        postLogoutRedirectUri = "https://www.nav.no"
        setAsDefault = true
    }
    
    routing {
        authenticate {
            get("/sikret") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
```

Etter bruker her har logget inn i idporten vil den få et jwt token lagret i cookie 'user_id_token'. 
Denne cookien er scopet til domene og rootpath for appen.

## IdportenUser

Biblioteket tilbyr også en måte å pakke ut informasjon fra en autorisert brukers token.

Dette kan gjøres som følger:

```kotlin
authenticate(IdPortenCookieAuthenticator.name) {
    get("/sikret") {
        val user = IdportenUserFactory.createNewAuthenticatedUser(call)
        call.respond("Du er logger inn som $user.")
    }
}
```

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket forventer at følgende miljøvariabler er satt:

- IDPORTEN_WELL_KNOWN_URL
- IDPORTEN_CLIENT_ID
- IDPORTEN_CLIENT_JWK
- IDPORTEN_REDIRECT_URI

Når nais-yaml er konfigurert riktig settes disse av plattformen ved kjøring i miljø. Ved lokal kjøring må disse også være satt. 

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/idporten/#runtime-variables-credentials) for nærmere forklaring.
