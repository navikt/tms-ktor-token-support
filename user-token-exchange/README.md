# user-token-exchange

Dette biblioteket tilbyr en måte for en app å veksle brukertokens mot tokendings.

## Nais-yaml

Bruk av biblioteket forutsetter at nais-yaml er konfigurert for tokenx:

```yaml
spec:
  tokenx:
    enabled: true
```

For å kunne veksle tokens og kalle andre apper er det også nødvendig å konfigurere dette i nais-yaml. 
Både navn på ønsket api og appens ingress (uten scheme) må defineres:

```yaml
spec:
  accessPolicy:
    outbound:
      rules:
        - application: other-api
      external:
        - host: other-api.nav.no
```

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/tokenx/#access-policies) for nærmere forklaring på adgangsstyring mellom apper.

## Oppsett

Biblioteket tilbyr ett interface `UserTokenExchangeService` med to implementasjoner, `CachingExchangeService` og `NonCachingExchangeService`.
Disse bygges ved hjelp av `UserTokenExchangeBuilder`: 

```kotlin
fun Application.setup() {

    val serviceWithCache = UserTokenExchangeBuilder.buildExchangeService(
        cachingEnabled = true,
        maxCachedEntries = 100,
        cacheMarginSeconds = 10
    )
   
    val serviceWithoutCache = TokendingsServiceBuilder.buildExchangeService(
        cachingEnabled = false,
    )
}
```

### Caching

Grunnet potensiell høy trafikk mot tokendings anbefales det å ikke skru av caching av access tokens i `UserTokenExchangeService`.

Default instillinger i `UserTokenExchangeBuilder` er som følger: 
```kotlin
cachingEnabled = true
maxCachedEntries = 1000
cacheMarginSeconds = 5
```

`cacheMarginSeconds` bestemmer hvor lenge før access-tokenet egentlig utløper at vi invaliderer det fra cachen, for 
å kompensere for potensielle tregheter.   

Caching gjøres med `sub` claim på original token som nøkkel. 

## Bruk

`UserTokenExchangeService` brukes til å veksle tokens med tokendings. For å gjøre dette må en først ha et bruker-token fra
ID-porten eller tokendings som en ønsker å veksle. En må spesifisere hvilken app vekslet token er ment for i formatet `<cluster>:<namespace>:<appnavn>`.

Eksempel på tokenveksling for å nå en app i cluster `prod-gcp` og namespace `min-side`:

```kotlin
fun getTokenForOtherApi(subjectToken: String): String {
    val appName = "prod-gcp:min-side:other-api"

    val tokendingsService = TokendingsServiceBuilder.buildTokendingsService()

    return tokendingsService.exchangeToken(subjectToken, appName)
}
```

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket forventer at følgende miljøvariabler er satt:

- TOKEN_X_WELL_KNOWN_URL
- TOKEN_X_CLIENT_ID
- TOKEN_X_PRIVATE_JWK

Når nais-yaml er konfigurert riktig settes disse av plattformen ved kjøring i miljø. Ved lokal kjøring må disse også være satt.

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/tokenx/#runtime-variables-credentials) for forklaring på disse miljøvariablene.
