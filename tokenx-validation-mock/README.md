# tokenx-validation-mock

Dette biblioteket kan installeres i stedet for `tokenx-validation` for å simulere innlogging.

Kun ment å brukes for testing, og bør ikke havne i miljø.

## Oppsett 

For å kunne autentisere et endepunkt må man først installere autentikatoren.

Denne har en rekke = variabler:

- `authenticatorName`: Bestemmer navnet på autentikatoren. Default `TokenXAuthenticator.name`
- `setAsDefault`: (Optional) Setter denne autentikatoren som default. Default 'false'
- `alwaysAuthenticated`: (Optional) Bestemmer om alle kall skal være godkjent eller motsatt. Default 'false'
- `staticLevelOfAssurance`: Bestemmer hvilket innloggingsnivå bruker er logget inn med. Default 'null'.
- `staticUserPid`: Bestemmer hvilken ident bruker er logget inn med. Default 'null'.
- `staticJwtOverride`: Bestemmer hvilket token som evt skal settes i AzurePrincipal. Default 'null'.

Dersom alwaysAuthenticated er 'true', må enten 'staticLevelOfAssurance' og 'staticUserPid' være satt, eller så
må 'staticJwtOverride' være satt. 'staticJwtOverride' må ha claims 'acr' (satt til `idporten-loa-substantial` eller `idporten-loa-high`) og 'pid'.
Dersom alle feltene er satt er det 'staticJwtOverride' som er gjeldende.

Eksempel på konfigurasjon:

```kotlin
fun Application.setup() {

    authentication {
        tokenXMock { 
            setAsDefault = false
            alwaysAuthenticated = true
            staticLevelOfAssurance = HIGH
            staticUserPid = '123'
        }
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Hvis ikke denne autentikatoren er satt som default, er det
viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.setup() {

    authentication {
        tokenX {
            setAsDefault = false
        }
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

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket krever ingen miljøvariabler. Dette biblioteket skal ikke brukes i miljø.
