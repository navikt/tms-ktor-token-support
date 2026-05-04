# entra-id-token-verification-mock

Dette biblioteket kan installeres i stedet for `entra-id-token-verification` for å simulere innlogging.

Kun ment å brukes i tester, og skal ikke havne i miljø.

## Oppsett

For å kunne autentisere et endepunkt må man først installere autentikatoren:

```kotlin
fun Application.setup() {

    authentication {
        entraIdMock {
            
        }
    }
}
```


Deretter kan man autentisere bestemte endepunkt som følger.

```kotlin
fun Application.setup() {

    authentication {
        entraIdMock {

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

### Mocked autentisering

Når en kaller et autentisert endepunkt kan man enten sende mocked autentisering via header, eller sette det som default for hele autentikatoren:

```kotlin
fun Application.setup() {

    authentication {
        entraIdMock("config_1") {
            enableDefaultAuthentication {
                
            }
        }
        entraIdMock("config_2") {

        }
    }
    
    routing {
        authenticate("config_1") {
            get("/sikret-1") {
                call.respond(HttpStatusCode.OK)
            }
        }
        authenticate("config_2") {
            get("/sikret-2") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun test() {
    // Uten ytterligere autentisering
    client.get("/sikret-1").status shouldBe OK
    client.get("/sikret-2").status shouldBe Unauthorized
    
    // Med autentisering i header
    client.get("/sikret-1") {
        mockAuthorizedHeader()
    }.status shouldBe OK
    
    client.get("/sikret-2") {
        mockAuthorizedHeader()
    }.status shouldBe OK
}
```

### Konfigurasjon av autentisering

En kan også styre innholded i mocked autentisering.

Dersom ikke annet er gitt simulerer biblioteket et system-token utstedt til app: `test-cluster:test-namespace:test-app_(default|header)-provided`.

En kan overstyre dette med å velge annen issuedFor:

```kotlin
fun Application.setup() {

    authentication {
        entraIdMock("config_1") {
            enableDefaultAuthentication {
                tokenIssuedFor = NaisApplication("en", "annen", "app")
            }
        }
    }
}
```

Dersom en oppggir en mennesklig brukers info vil biblioteket simulere et obo-token:

```kotlin
fun Application.setup() {

    authentication {
        entraIdMock("config_1") {
            enableDefaultAuthentication {
                tokenUserInfo = UserInfo(
                    navIdent = "A000000",
                    userId = "111",
                    displayName = "Navn",
                    userName = "navn@nav.no"
                )
            }
        }
    }
}
```

## EntraIdPrincipal

Informasjon i token om hvem det er utstedt til, og evt. på vegne av, havner i EntraIdPrincipal/EntraIdUserPrincipal på
samme måte som i vanlig modul.

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket krever ingen miljøvariabler. Dette biblioteket skal ikke brukes i miljø.
