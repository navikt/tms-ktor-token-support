# token-support-idporten

Dette biblioteket tilbyr en måte for en ktor app å autentisere en bruker mot idporten.

## Oppsett

Bruk av biblioteket forutsetter at nais-yaml er konfigurert for idporten:

```yaml
spec:
  idporten:
    enabled: true
```

For å kunne autentisere et endepunkt må man først installere autentikatoren.

Her er det 5 variabler:

`tokenCookieName`: (Required) Navn på token-cookien som settes i browser etter bruker har logget inn.
`postLogoutRedirectUri`: (Required) Bestemmer hvor bruker sendes etter de har logget ut. Denne må samsvare med nais-yaml.
`postLoginRedirectUri`: (Optional) Url der bruker havner etter login dersom vi ikke finner en "redirect_uri" cookie. Default ""
`setAsDefault`: (Optional) Setter denne autentikatoren som default. Default 'false'
`secureCookie`: (Optional) Setter token-cookie som secure, slik at den kun sendes med https-kall. Bør kun skrus av ved lokal kjøring. Default 'true'
`alwaysRedirectToLogin`: (Optional) Bestemmer om beskyttede endepunkt kan sende bruker til ID-porten, eller om de kun svarer med status 401. Default 'false'
`securityLevel`: (Optional) Setter minimum sikkerhetsnivå for alle innlogginger. Default 'NOT_SPECIFIED' 
 
Eksempel på konfigurasjon:

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        tokenCookieName = "user_id_token"
        postLogoutRedirectUri = "https://www.nav.no"
        postLoginRedirectUri = '/post/login'
        setAsDefault = false
        secureCookie = true
        alwaysRedirectToLogin = false
        securityLevel = SecurityLevel.LEVEL_3
    }
}
```

Deretter kan man autentisere bestemte endepunkt som følger. Det er viktig å ha med navnet på autentikatoren.

```kotlin
fun Application.mainModule() {

    installIdPortenAuth {
        tokenCookieName = "user_id_token"
        postLogoutRedirectUri = "https://www.nav.no"
        postLoginRedirectUri = '/post/login'
        setAsDefault = false
        secureCookie = true
        alwaysRedirectToLogin = false
        securityLevel = SecurityLevel.LEVEL_3
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
fun Application.mainModule() {

    installIdPortenAuth {
        tokenCookieName = "user_id_token"
        postLogoutRedirectUri = "https://www.nav.no"
        setAsDefault = true
        securityLevel = SecurityLevel.LEVEL_4
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

Etter bruker her har logget inn i idporten vil den få et jwt token lagret i cookie 'user_id_token'. 
Denne cookien er scopet til domene og rootpath for appen.

## Redirect vs http status 401

---

Når `securityLevel` er satt til 'false', vil beskyttede endepunkt svare med http-status 401 dersom bruker ikke
har et gyldig ID-token. For at bruker skal bli sendt til ID-porten må bruker først innom endepunktet `/login`. 
Dette endepunktet tillater å overstyre endelig redirect etter login med parameteren `redirect_uri`. Biblioteket 
tilbyr også et endepunkt `/login/statsus` som viser om bruker er logget inn eller ikke, og evt på hvilket nivå.

Eksempel oppsett med js-frontend 'app-1' og ktor backend 'app-2' der dette biblioteket er installert. 

1. app-1 kaller `<app-2>/login/status` hos app-2 og ser at bruker ikke er logget inn.

2. app-1 sender bruker til `<app-2>/login?redirect_uri=<app-1>`

3. Bruker blir sendt til ID-porten, logger inn, og blir sendt tilbake til app-2

4. app-2 sender bruker tilbake til app-1

---

Når `securityLevel` er satt til 'true', vil alle beskyttede endepunk kunne svare med en redirect til ID-porten
dersom bruker ikke har et gyldig id-token. Dette passer best når biblioteket er installert på en frontend ktor-app.

## IdportenUser

Biblioteket tilbyr også en måte å pakke ut informasjon fra en autorisert brukers token.

Dette kan gjøres som følger:

```kotlin
authenticate(IdPortenCookieAuthenticator.name) {
    get("/sikret") {
        val user = IdportenUserFactory.createNewAuthenticatedUser(call)
        call.respond("Du er logger inn som $user.")
    }
}
```

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket forventer at følgende miljøvariabler er satt:

- IDPORTEN_WELL_KNOWN_URL
- IDPORTEN_CLIENT_ID
- IDPORTEN_CLIENT_JWK
- IDPORTEN_REDIRECT_URI

Når nais-yaml er konfigurert riktig settes disse av plattformen ved kjøring i miljø. Ved lokal kjøring må disse også være satt. 

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/idporten/#runtime-variables-credentials) for nærmere forklaring.
