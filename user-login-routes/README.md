# idporten-sidecar

Dette biblioteket tilbyr en måte for en ktor app å verifisere tokens utstedt fra idporten eller tokendings.

## Oppsett

Bruk av biblioteket forutsetter at nais-yaml er konfigurert for idporten-sidecar og/eller tokenx:

```yaml
spec:
  idporten:
    enabled: true
    sidecar:
      enabled: true
```

```yaml
spec:
  tokenx:
    enabled: true
```

Les docen flere konfigurasjonsmuligheter for [idporten](https://docs.nais.io/auth/idporten/) og [tokenx](https://docs.nais.io/auth/tokenx/).


For å kunne autentisere et endepunkt må man først installere autentikatoren.

Autentikatoren vil automatisk legge til Idporten og/eller Tokendings som gyldig issuer basert på appens nais-config.
En kan overstyre dette ved å velge godkjent issuer manuelt:

```kotlin
fun Application.setup() {
    
    authentication {
        userToken {
            acceptIssuer(IdPorten, Tokendings)
        }
    }
}
```

Det finnes også en rekke konfigurasjonsmuligheter:

- `authenticatorName`: Bestemmer navnet på autentikatoren. Default `UserTokenAuthenticator.name`
- `setAsDefault`: (Optional) Setter denne autentikatoren som default. Default 'false'
- `levelOfAssurance`: (Optional) Setter minimum level-of-assurance for endepunkt. Default 'HIGH'
- `enableDefaultProxy`: (Optional) Bestemmer hvorvidt system-default proxy skal brukes ved kall mot andre tjenester. Nødvendig for on-prem apper med webproxy. Default 'false'.
 
Eksempel på konfigurasjon:

```kotlin
fun Application.setup() {
    
    authentication {
        userToken {
            setAsDefault = false
            levelOfAssurance = LevelOfAssurance.SUBSTANTIAL
            enableDefaultProxy = false
        }
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Det er viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.setup() {

    authentication {
        userToken {
            setAsDefault = false
            levelOfAssurance = LevelOfAssurance.SUBSTANTIAL
            enableDefaultProxy = false
        }
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
fun Application.setup() {

    authentication {
        userToken {
            setAsDefault = true
            levelOfAssurance = LevelOfAssurance.HIGH
        }
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

Eksempel på bruk der ulike endepunkt krever ulike nivå:

```kotlin
fun Application.setup() {

    authentication {
        userToken {
            setAsDefault = true
            levelOfAssurance = LevelOfAssurance.HIGH
        }
        
        userToken {
            authenticatorName = "lavere_krav"
            levelOfAssurance = LevelOfAssurance.SUBSTANTIAL
            setAsDefault = false
        }
    }
    
    routing {
        authenticate {
            get("/sikret") {
                call.respond(HttpStatusCode.OK)
            }
        }
        
        authenticate("lavere_krav") {
            get("/sikret/lavere") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
```

## Login plugin

Bilbioteket tilbyr en plugin `IdPortenLogin` for apper som har satt opp integrasjon mot idporten. 

Denne legger til to endepunkt for å fasilitere innloggingsflyt:

- `/login`: Setter i gang innlogging hos ID-porten via sidecar. 
- `/login/status`: Viser status for innlogging - om bruker er innlogget og med hvilket nivå.

Plugin har to variabler:

- `enableDefaultProxy`: (Optional) Bestemmer hvorvidt system-default proxy skal brukes ved kall mot andre tjenester. Default 'false'.
- `routesPrefix`: (Optional) Bestemmer en relativ path der endepunktene plasseres. Default 'null'.

Eksempel på oppsett:

```kotlin
fun Application.setup() {
    install(IdPortenLogin) {
        routesPrefix = '/implicit/root/path'
        enableDefaultProxy = false
    }
}
```

Eksempel på bruk:

Installere Plugin:

```kotlin
fun Application.setup() {
    install(IdPortenLogin)
}
```

Initiere login på med 'substantial' level of assurance:

`https://backend.nav.no/login?loa=substantial&redirect_uri=https://frontend.nav.no`

Sjekke status etter innlogging

`https://backend.nav.no/login/status`

svarer med:
```json
{
  "authenticated": true,
  "level": 3,
  "levelOfAssurance": "substantial"
}
```

## UserPrincipal

Biblioteket tilbyr også en måte å pakke ut informasjon fra en autorisert brukers token.

Dette kan gjøres som følger:

```kotlin
authenticate {
    get("/sikret") {
        val user = UserPrincipalFactory.getAuthenticatedUser(call)
        call.respond("Du er logget inn som $user.")
    }
}
```

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket forventer at følgende miljøvariabel er satt:

- IDPORTEN_WELL_KNOWN_URL

Når nais-yaml er konfigurert riktig settes disse av plattformen ved kjøring i miljø. Ved lokal kjøring må disse også være satt. 

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/idporten/#runtime-variables-credentials) for nærmere forklaring.
