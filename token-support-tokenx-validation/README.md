# token-support-tokenx-validation

Dette biblioteket tilbyr en måte for en ktor app å verifisere bearer tokens vekslet fra tokendings.

## Nais-yaml

Bruk av biblioteket forutsetter at nais-yaml er konfigurert for tokenx:

```yaml
spec:
  tokenx:
    enabled: true
```


## Oppsett 

For å kunne autentisere et endepunkt må man først installere autentikatoren.

Denne har 1 variabel:

`setAsDefault`: (Optional) Setter denne autentikatoren som default. Default 'false'
 
Eksempel på konfigurasjon:

```kotlin
fun Application.mainModule() {

    installTokenXAuth {
        setAsDefault = false
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Hvis denne autentikatoren er satt som default, er det
viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.mainModule() {

    installTokenXAuth {
        setAsDefault = false
    }
    
    routing {
        authenticate(TokenXAuthenticator.name) {
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

    installTokenXAuth {
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

Alle endepunkt som er sikret på denne måten vil kreve at http-kall sender et gyldig jwt som Bearer-token
i Authorization headeren. Cookie støttes ikke. Ugyldige kall vil alltid svares med en 401-feilkode.

## TokenXUser

Biblioteket tilbyr også en måte å pakke ut informasjon fra en autorisert brukers token.

Dette kan gjøres som følger:

```kotlin
authenticate(TokenXAuthenticator.name) {
    get("/sikret") {
        val user = TokenXUserFactory.createNewAuthenticatedUser(call)
        call.respond("Du er logger inn som $user.")
    }
}
```

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket forventer at følgende miljøvariabler er satt:

- TOKEN_X_WELL_KNOWN_URL
- TOKEN_X_CLIENT_ID

Når nais-yaml er konfigurert riktig settes disse av plattformen ved kjøring i miljø. Ved lokal kjøring må disse også være satt. 

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/tokenx/#runtime-variables-credentials) for nærmere forklaring.
