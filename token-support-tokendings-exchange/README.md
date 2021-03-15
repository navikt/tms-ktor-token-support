# token-support-idporten

Dette biblioteket tilbyr en måte for en ktor app å veksle id_tokens og access_tokens mot tokendings.


## Oppsett

Bruk av biblioteket forutsetter at nais-yaml er konfigurert for tokenx:

```yaml
spec:
  tokenx:
    enabled: true
```

For å kunen veksle tokens og kalle andre apper er det også nødvendig å konfigurere dette i nais-yaml. 
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

Biblioteket tilbyr ett interface `TokendingsService` med to implementasjoner, `CachingTokendingsService` og `NonCachingTokendingsService`.
Disse bygges ved hjelp av `TokendingsServiceBuilder`: 

```kotlin
fun Application.mainModule() {

    val serviceWithCache = TokendingsServiceBuilder.buildTokendingsService(
        cachingEnabled = true,
        maxCachedEntries = 100,
        cacheMarginSeconds = 10
    )
   
    val serviceWithoutCache = TokendingsServiceBuilder.buildTokendingsService(
        cachingEnabled = false,
    )
}
```

### Caching

Grunnet potensiell høy trafikk mot tokendings anbefales det å ikke skru av caching i `TokendingsService`.

Default instillinger i `TokendingsServiceBuilder` er som følger: 
```kotlin
cachingEnabled = true
maxCachedEntries = 1000
cacheMarginSeconds = 5
```

`cacheMarginSeconds` bestemmer hvor lenge før access-tokenet egentlig utløper at vi invaliderer det fra cachen, for 
å kompensere for potensielle tregheter.   

Caching gjøres med `sub` claim på original token som nøkkel. 

## Bruk

`TokendingsService` brukes til å veksle tokens med tokendings. For å gjøre dette må en først ha et id_token eller access_token
fra ID-porten eller tokendings som en ønsker å veksle. En må spesifisere hvilken app vekslet token er ment for i formatet `<cluster>:<namespace>:<appnavn>`.

Eksempel på tokenveksling for å nå en app i samme cluster og namespace:

```kotlin
fun getTokenForOtherApi(subjectToken: String): String {
    val appName = "cluster:namespace:other-api"
   
    return TokenExchangeServices.tokendingsService.exchangeToken(subjectToken, appName)
}
```

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket forventer at følgende miljøvariabler er satt:

- NAIS_CLUSTER_NAME
- NAIS_NAMESPACE
- TOKEN_X_WELL_KNOWN_URL
- TOKEN_X_CLIENT_ID
- TOKEN_X_PRIVATE_JWK

Når nais-yaml er konfigurert riktig settes disse av plattformen ved kjøring i miljø. Ved lokal kjøring må disse også være satt.

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/tokenx/#runtime-variables-credentials) for forklaring på de 3 siste av disse miljøvariablene.
