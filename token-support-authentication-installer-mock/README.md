# token-support-authentication-installer

Denne modulen tilbyr en måte å installere flere av bibliotekets autentikator-mocks i parallell.


## Nais-yaml

Krav for nais-yaml spesifikasjon er avhengig av hvilke autentikatorer som skal installeres. Referer til README for
autentikatorer som skal installeres.

## Oppsett

Biblioteket tilbyr en funksjon `Application.installMockedAuthenticators` lar en installere autentikatorer slik det gjøres 
i andre moduler:

```kotlin
fun Application.mainModule() {

    installMockedAuthenticators {
        installIdPortenAuthMock {
            ...
        }

        installTokenXAuthMock {
            ...
        }
    }
}
```

Dette gjør det mulig å velge autentikator for beskyttede endepunkt som følger:

```kotlin
fun Application.mainModule() {

    installAuthenticators {
        installIdPortenAuthMock {
            ...
        }

        installTokenXAuthMock {
            ...
        }
    }
    
    routing {
        authenticate(IdPortenCookieAuthenticator.name) {
            get("/sikretForIdPorten") {
                call.respond(HttpStatusCode.OK)
            }
        }
        
        authenticate(TokenXAuthenticator.name) {
            get("/sikretForTokenX") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
```

Referer til README til hver enkelt autentikator for mer informasjon om oppsett og bruk.

### Default authenticator

Det er også mulig å velge opp til 1 autentikator som default, slik at den ikke trenger å navngis for hvert sikrede endepunkt.

Eksempel for oppsett og bruk:

```kotlin
fun Application.mainModule() {

    installAuthenticators {
        installIdPortenAuthMock {
            ...
            setAsDefault = true
        }

        installTokenXAuthMock {
            ...
        }
    }
    
    routing {
        authenticate {
            get("/sikretForIdPorten") {
                call.respond(HttpStatusCode.OK)
            }
        }
        
        authenticate(TokenXAuthenticator.name) {
            get("/sikretForTokenX") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
```

## Bruk av biblioteket ved lokal kjøring 

Som andre mock-moduler skal denne ikke kjøres i miljø.
