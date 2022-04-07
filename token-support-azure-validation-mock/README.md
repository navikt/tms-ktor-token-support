# token-support-azure-validation-mock

Dette biblioteket kan installeres i stedet for `token-support-azure-validation` for å simulere innlogging.

Kun ment å brukes for testing, og bør ikke havne i miljø.

## Oppsett

For å kunne autentisere et endepunkt må man først installere autentikatoren.

Denne har 3 variabler:

- `setAsDefault`: (Optional) Setter denne autentikatoren som default. Default 'false'
- `alwaysAuthenticated`: (Optional) Bestemmer om alle kall skal være godkjent eller motsatt. Default 'false'
- `staticJwtOverride`: (Optional) Bestemmer hvilket token som evt skal settes i AzurePrincipal. Default 'null'.

Eksempel på konfigurasjon:

```kotlin
fun Application.mainModule() {

    installAzureAuthMock {
        setAsDefault = false
        alwaysAuthenticated = false
        staticJwtOverride = null
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Hvis ikke denne autentikatoren er satt som default, er det
viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.mainModule() {

    installAzureAuthMock {
        setAsDefault = false
    }
    
    routing {
        authenticate(AzureAuthenticator.name) {
            get("/sikret") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
```

Alle endepunkt som bruker denne autentikatoren vil enten svare 401 eller godkjenne koblingen basert på alwaysAuthenticated.

## AzurePrincipal

Autentikatoren kan settes opp med en default jwt-string, som legges i AzurePrincipal. 

Som default legges det en usignert jwt med enkle claims.

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket krever ingen miljøvariabler. Dette biblioteket skal ikke brukes i miljø.
