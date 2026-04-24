# user-token-verification-mock

Dette biblioteket kan installeres i stedet for `user-token-verification` for å simulere innlogging.

Kun ment å brukes i tester, og skal ikke havne i miljø.

## Oppsett

For å kunne autentisere et endepunkt må man først installere autentikatoren:

```kotlin
fun Application.setup() {

    authentication {
        userTokenMock {
            
        }
    }
}
```


Deretter kan man autentisere bestemte endepunkt som følger.

```kotlin
fun Application.setup() {

    authentication {
        userTokenMock {

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

Når en kaller et autentisert endepunkt kan man enten sende mocked autentisering via header, eller sette det som default for hele autentikatoren.

En må oppgi tokenets issuer (Tokenx eller IdPorten), og simulert brukers ident

```kotlin
fun Application.setup() {

    authentication {
        userTokenMock("config_1") {
            enableDefaultAuthentication {
                tokenIdent = "123"
                tokenIssuer = Issuer.IdPorten
            } 
        }
        userTokenMock("config_2") {

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
        mockAuthorizedHeader(
            ident = "123",
            issuer = Issuer.IdPorten
        )
    }.status shouldBe OK
    
    client.get("/sikret-2") {
        mockAuthorizedHeader(
            ident = "123",
            issuer = Issuer.IdPorten
        )
    }.status shouldBe OK
}
```

### Issuers

Dersom ikke noe annet er oppgitt, regnes både IdPorten og Tokenx som gyldige issuers.

Dette kan en endre ved konfigurasjon av autentikator:

```kotlin
fun Application.setup() {

    authentication {
        userTokenMock {
            configureIssuers(Issuer.IdPorten)
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

fun test() {
    client.get("/sikret") {
        mockAuthorizedHeader(
            ident = "123",
            issuer = Issuer.IdPorten
        )
    }.status shouldBe OK

    client.get("/sikret") {
        mockAuthorizedHeader(
            ident = "123",
            issuer = Issuer.Tokenx
        )
    }.status shouldBe Unauthorized
}
```

Når en spesifiserer 1 gyldig issuer, trenger en ikke gjenta denne ved oppsett av default autentisering:

```kotlin
fun Application.setup() {

    authentication {
        userTokenMock {
            configureIssuers(Issuer.IdPorten)

            enableDefaultAuthentication {
                tokenIdent = "123"
            }
        }
    }
}
```

Det vil også feile dersom issuer i default autentisering ikke samsvarer med påkrevd issuer.

### Level og assurance

En kan også styre påkrevd og default level of assurance:

```kotlin
fun Application.setup() {

    authentication {
        userTokenMock {
            configureIssuers(Issuer.IdPorten)
            levelOfAssurance = LevelOfAssurance.Substantial

            enableDefaultAuthentication {
                tokenIdent = "123"
                tokenLoa = LevelOfAssurance.High
            }
        }
    }
}
```

Som med issuers vil dette valideres ved runtime. En kan ikke sette default lavere enn påkrevd, og en vil få 401 dersom
en supplerer autentisering med for lav loa i header.

### UserTokenPrincipal 

Informasjon brukers inlogging havner i UserTokenPrincipal som i vanlig bibliotek.

## Bruk av biblioteket ved lokal kjøring

Dette biblioteket krever ingen miljøvariabler. Dette biblioteket skal ikke brukes i miljø.
