# token-support-idporten-validation

Dette biblioteket kan installeres i stedet for `token-support-idporten-validation` for å simulere innlogging.

Kun ment å brukes for testing, og bør ikke havne i miljø.

## Oppsett 

For å kunne autentisere et endepunkt må man først installere autentikatoren.

Denne har 1 variabel:

`setAsDefault`: (Optional) Setter denne autentikatoren som default. Default 'false'
`alwaysAuthenticated`: (Optional) Bestemmer om alle kall skal være godkjent eller motsatt. Default 'false'
`staticSecurityLevel`: Bestemmer hvilket innloggingsnivå bruker er logget inn med. Default 'null'.
`staticUserPid`: Bestemmer hvilken ident bruker er logget inn med. Default 'null'.
`staticJwtOverride`: Bestemmer hvilket token som evt skal settes i AzurePrincipal. Default 'null'.

Dersom alwaysAuthenticated er 'true', må enten 'staticSecurityLevel' og 'staticUserPid' være satt, eller så
må 'staticJwtOverride' være satt. 'staticJwtOverride' må ha claims 'acr_values' (satt til `Level3` eller `Level4`) og 'pid'.
Dersom alle feltene er satt er det 'staticJwtOverride' som er gjeldende.

Eksempel på konfigurasjon:

```kotlin
fun Application.mainModule() {

    installIdPortenAuthMock {
        setAsDefault = false
        alwaysAuthenticated = true
        staticSecurityLevel = LEVEL_4
        staticUserPid = '123'
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Hvis ikke denne autentikatoren er satt som default, er det
viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        setAsDefault = false
    }
    
    routing {
        authenticate(IdPortenAuthenticator.name) {
            get("/sikret") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
```

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket krever ingen miljøvariabler. Dette biblioteket skal ikke brukes i miljø.

Biblioteket legger ikke til ekstra endepunkt som f. eks. '/login'
