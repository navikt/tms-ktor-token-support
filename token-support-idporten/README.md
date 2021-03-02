# token-support-idporten

Dette biblioteket en måte for en ktor app å autentisere en bruker mot idporten.

## Oppsett

For å kunne autentisere et endepunkt må man først installere autentikatorene.

Her er det 4 variabler:

`tokenCookieName`: (Required) Navn på token-cookien som settes i browser etter bruker har logget inn.
`postLoginRedirectUri`: (Optional) Url der bruker havner etter login dersom vi ikke finner en "redirect_uri" cookie. Default ""
`setAsDefaultAuthenticator`: (Optional) Setter denne autentikatoren som default. Default 'false'
`secureCookie`: (Optional) Setter token-cookie som secure, slik at den kun sendes med https-kall. Default 'true'
 
Eksempel på konfigurasjon:

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        tokenCookieName = "user_id_token"
        postLoginRedirectUri = '/post/login'
        setAsDefaultAuthenticator = false
        secureCookie = true
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Det er viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        tokenCookieName = "user_id_token"
        postLoginRedirectUri = '/post/login'
        setAsDefaultAuthenticator = false
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
        setAsDefaultAuthenticator = true
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
