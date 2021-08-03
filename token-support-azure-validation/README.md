# token-support-azure-validation

Dette biblioteket tilbyr en måte for en ktor app å verifisere bearer tokens utstedt fra azure.

## Nais-yaml

Bruk av biblioteket forutsetter at nais-yaml er konfigurert for azure:

```yaml
spec:
  azure:
    application:
      enabled: true
```

## Oppsett

For å kunne autentisere et endepunkt må man først installere autentikatoren.

Denne har 1 variabel:

`setAsDefault`: (Optional) Setter denne autentikatoren som default. Default 'false'
 
Eksempel på konfigurasjon:

```kotlin
fun Application.mainModule() {

    installAzureAuth {
        setAsDefault = false
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Hvis ikke denne autentikatoren er satt som default, er det
viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.mainModule() {

    installAzureAuth {
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

Typisk eksempel på bruk i miljø og dette er default authenticator:

```kotlin
fun Application.mainModule() {

    installAzureAuth {
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

## AzurePrincipal

Dette biblioteket er i utgangspunktet ment for bruk ved client-client kommunikasjon uten at en bruker er involvert. 
Derfor er det ikke implementert en måte å bygge et "AzureUser" objekt, slik det er i andre moduler.

Dersom det er behov for å hente ut bestemte claims fra mottatt access token, er AzurePrincipal eksponert til brukere av biblioteket.

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket forventer at følgende miljøvariabler er satt:

- AZURE_APP_WELL_KNOWN_URL
- AZURE_APP_CLIENT_ID

Når nais-yaml er konfigurert riktig settes disse av plattformen ved kjøring i miljø. Ved lokal kjøring må disse også være satt. 

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/azure-ad/index.html#runtime-variables-credentials) for nærmere forklaring.
