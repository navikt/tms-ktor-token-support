# token-support-idporten

Dette biblioteket en måte for en ktor app å autentisere en bruker mot idporten.

## Oppsett

For å kunne autentisere et endepunkt må man først installere autentikatorene.

Ved konfigurering er det nødvendig å spesifisere navn på cookien der brukerens token til slutt vil havne.
Det er også mulig å bestemme et fallback-endepunkt som bruker ledes til etter login. Dette er valgfritt, 
og bruker vil vanligvis ledes tilbake til det autentiserte endepunktet de først traff. 

Eksempel på konfigurasjon:

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        tokenCookieName = "user_id_token"
        postLoginRedirectUri = '/post/login'
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Det er viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        tokenCookieName = "user_id_token"
        postLoginRedirectUri = '/post/login'
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

Etter bruker her har logget inn i idporten vil den få et jwt token lagret i cookie 'user_id_token'. 
Denne cookien er scopet til domene og rootpath for appen.

## IdportenUser

Biblioteket tilbyr også en måte å hente informasjon om en autentisert brukers token i autentiserte endepunkt. 

Dette kan gjøres som følger:

```kotlin
authenticate(IdPortenCookieAuthenticator.name) {
    get("/sikret") {
        val user = IdportenUserFactory.createNewAuthenticatedUser(call)
        call.respond("Du er logger inn som $user.")
    }
}
```
