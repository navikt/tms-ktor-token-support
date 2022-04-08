# token-support-authentication-installer

Denne modulen tilbyr en måte å installere flere av bibliotekets autentikatorer i parallell. Det er ikke nødvendig å 
bruke denne modulen dersom en kun ønsker å installere én autentikator.


## Nais-yaml

Krav for nais-yaml spesifikasjon er avhengig av hvilke autentikatorer som skal installeres. Referer til README for
autentikatorer som skal installeres.

## Oppsett

Biblioteket tilbyr en funksjon `Application.installAuthenticators` lar en installere autentikatorer slik det gjøres 
i andre moduler:

```kotlin
fun Application.mainModule() {
    
    installMockAuthenticators {
        installIdPortenAuth {
            ...
        }

        installTokenXAuth {
            ...
        }
    }
}
```

Dette gjør det mulig å velge autentikator for beskyttede endepunkt som følger:

```kotlin
fun Application.mainModule() {

    installAuthenticators {
        installIdPortenAuth {
            ...
        }

        installTokenXAuth {
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
        installIdPortenAuth {
            ...
            setAsDefault = true
        }

        installTokenXAuth {
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

Denne modulen krever ingen ting ekstra for å kunne kjøres lokalt. Referer til README for autentikatorer som skal 
installeres for mer info.
