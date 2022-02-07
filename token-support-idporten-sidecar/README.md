# token-support-idporten-sidecar

Dette biblioteket tilbyr en måte for en ktor app å samhandle med idporten-sidecar for å autentisere brukere.

## Oppsett

Bruk av biblioteket forutsetter at nais-yaml er konfigurert for idporten og sidecar:

```yaml
spec:
  idporten:
    enabled: true
    sidecar:
      enabled: true
```

Les docen flere konfigurasjonsmuligheter for [idporten](https://doc.nais.io/security/auth/idporten) og [sidecar](https://doc.nais.io/security/auth/idporten/sidecar/).


For å kunne autentisere et endepunkt må man først installere autentikatoren.

Her er det en rekke variabler:

- `setAsDefault`: (Optional) Setter denne autentikatoren som default. Default 'false'
- `loginLevel`: (Optional) Setter minimum sikkerhetsnivå for alle innlogginger. Default 'LEVEL_4'
- `postLoginRedirectUri`: (Optional) Url der bruker havner etter login dersom vi ikke finner en "redirect_uri" cookie. Default ""
- `enableDefaultProxy`: (Optional) Bestemmer hvorvidt system-default proxy skal brukes ved kall mot andre tjenester. Nødvendig for on-prem apper med webproxy.
- `fallbackCookieEnabled`: (Optional) Bestemmer om token kan hentes fra cookie dersom vi ikke finner auth-header. Ment for lokal kjøring. Default 'false'.
- `fallbackCookieName`: (Optional/Required) Bestemmer hvilken cookie token skal leses fra. Må kun settes hvis fallback er enablet. 
 
Eksempel på konfigurasjon:

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        postLoginRedirectUri = '/post/login'
        setAsDefault = false
        loginLevel = LoginLevel.LEVEL_3
        enableDefaultProxy = false
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Det er viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        postLoginRedirectUri = '/post/login'
        setAsDefault = false
        loginLevel = LoginLevel.LEVEL_3
        enableDefaultProxy = false
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

Typisk eksempel på bruk i miljø i et fagområde som krever nivå 4 og dette er default authenticator:

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        setAsDefault = true
        loginLevel = LoginLevel.LEVEL_4
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

Dette biblioteket forventer at følgende miljøvariabel er satt:

- IDPORTEN_WELL_KNOWN_URL

Når nais-yaml er konfigurert riktig settes disse av plattformen ved kjøring i miljø. Ved lokal kjøring må disse også være satt. 

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/idporten/#runtime-variables-credentials) for nærmere forklaring.
