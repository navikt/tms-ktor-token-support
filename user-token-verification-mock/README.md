# idporten-sidecar-mock

Dette biblioteket kan installeres i stedet for `user-token-verification` for å simulere innlogging.

Kun ment å brukes for testing, og bør ikke havne i miljø.

## Oppsett 

For å kunne autentisere et endepunkt må man først installere autentikatoren.

Siden det ikke finnes et miljø å hente issuers fra må dette settes manuelt:

```kotlin
fun Application.setup() {
    
    authentication {
        userTokenMock {
            acceptIssuer(IdPorten, Tokendings)
        }
    }
}
```

Det finnes også finnes det en rekke konfigurasjonsmuligheter:

- `authenticatorName`: Bestemmer navnet på autentikatoren. Default `UserTokenAuthenticator.name`
- `setAsDefault`: (Optional) Setter denne autentikatoren som default. Default 'false'
- `alwaysAuthenticated`: (Optional) Bestemmer om alle kall skal være godkjent eller motsatt. Default 'false'
- `staticIssuer`: Bestemmer hvor mock-tokenet simulerer å komme fra. Default første i issuer-lister nevnt over.
- `staticLevelOfAssurance`: Bestemmer hvilket innloggingsnivå bruker er logget inn med. Default 'null'.
- `staticUserPid`: Bestemmer hvilken ident bruker er logget inn med. Default 'null'.
- `staticJwtOverride`: Bestemmer hvilket token som evt skal settes i UserPrincipal. Default 'null'.

Dersom alwaysAuthenticated er 'true', må enten 'staticLevelOfAssurance' og 'staticUserPid' være satt, eller så
må 'staticJwtOverride' være satt. 

Hvis en bruker 'staticJwtOverride', må det ha claims:

- 'acr': 
  - for issuer IdPorten: `idporten-loa-substantial` eller `idporten-loa-high`
  - for issuer TokenX: `Level3` eller `Level4`
- 'pid': Testbrukers ident
- 'iss':
  - for issuer IdPorten: `https://mock.idporten.no`
  - for issuer TokenX: `https://tokenx.mock`

Dersom alle feltene er satt er det 'staticJwtOverride' som er gjeldende.

Eksempel på konfigurasjon:

```kotlin
fun Application.setup() {

    authentication {
        userTokenMock {
            setAsDefault = true
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
        userTokenMock {
            setAsDefault = false
        }
    }
    
    routing {
        authenticate(UserTokenAuthenticator.name) {
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
