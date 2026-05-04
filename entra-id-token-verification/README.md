# entra-id-validation

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

For å kunne autentisere et endepunkt må man først installere autentikatoren:

```kotlin
fun Application.setup() {

    authentication {
        entraId {
            
        }
    }
}
```

Den kan konfigureres ytterligere som følgende:

- `enableDefaultProxy`: Bestemmer hvorvidt system-default proxy skal brukes ved kall mot andre tjenester. Nødvendig for on-prem apper med webproxy.
- `filterClients { ... }`: Tillater filtrering på konsumenter av endepunktet basert på appnavn, eller i hvilket miljø de kjører

Eksempel på tilfelle med to ulike konfigurasjoner:

```kotlin
fun Application.setup() {

    authentication {
        entraId {
            
        }
        
        entraId("internal_api") {
            filterClients {
                namespace = "min-side"
            }
        }
    }
}
```

Deretter kan man autentisere bestemte endepunkt på følgende måte:

```kotlin
fun Application.setup() {
    
    routing {
        authenticate {
            get("/sikret/ekstern") {
                call.respond(HttpStatusCode.OK)
            }
        }
        
        authenticate("internal_api") {
            get("/sikret/intern") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
```

## EntraIdPrincipal

Etter autentisering kan informasjon om konsumenten hentes fra context: 

```kotlin
fun Application.setup() {
    
    routing {
        authenticate {
            get("/sikret") {
                val principal = call.principal<EntraIdPrincipal>()
                
                ...
            }
        }
    }
}
```

Dersom token er tilstedt på vegne av bruker, kan en hente `EntraIdUserPrincipal` der det finnes noe mer informasjon:

```kotlin
fun Application.setup() {
    
    routing {
        authenticate {
            get("/sikret") {
                val principal = call.principal<EntraIdUserPrincipal>()
                
                auditLog.persistAction(action = "API_CALL", user = principal.navIdent)
                
                ...
            }
        }
    }
}
```

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket forventer at følgende miljøvariabler er satt:

- AZURE_APP_WELL_KNOWN_URL
- AZURE_APP_CLIENT_ID
- NAIS_CLUSTER_NAME
- NAIS_NAMESPACE
- NAIS_APP_NAME

Når nais-yaml er konfigurert riktig settes disse av plattformen ved kjøring i miljø. Ved lokal kjøring må disse også være satt.

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/azure-ad/index.html#runtime-variables-credentials) for nærmere forklaring.
