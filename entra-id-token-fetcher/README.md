# entra-id-token-fetcher

Dette biblioteket tilbyr en måte for en ktor app å hente entraid access tokens ment for autentisert kommunikasjon uten innbygger-token.


## Nais-yaml

Bruk av biblioteket forutsetter at nais-yaml er konfigurert for azure:

```yaml
spec:
  azure:
    application:
      enabled: true
```

## Oppsett

Biblioteket tilbyr ett interface `EntraIdTokenFetcher` med to implementasjoner, `CachingEntraIdTokenFetcher` og `NonCachingEntraIdTokenFetcher`.
Disse bygges ved hjelp av `EntraIdTokenFetcherBuilder`: 

```kotlin
fun Application.setup() {

    val fetcherWithCache = EntraIdTokenFetcherBuilder.buildFetcher(
        cachingEnabled = true,
        maxCachedEntries = 100,
        cacheMarginSeconds = 10,
        enableDefaultProxy = true

    )
   
    val serviceWithoutCache = EntraIdTokenFetcherBuilder.buildFetcher(
        cachingEnabled = false,
    )
}
```

Default proxy er nødvendig dersom appen kjører on-prem med webproxy.

### Caching

Grunnet potensiell høy trafikk mot token-issuer er det ikke anbefalt å skru av caching i `EntraIdTokenFetcher`.

Default instillinger i `EntraIdTokenFetcherBuilder` er som følger: 

```kotlin
cachingEnabled = true
maxCachedEntries = 1000
cacheMarginSeconds = 5
enableDefaultProxy = false
```

`cacheMarginSeconds` bestemmer hvor lenge før access-tokenet egentlig utløper at vi invaliderer det fra cachen, for 
å kompensere for potensielle tregheter.   

Caching gjøres med clientnavn på audience som nøkkel. 

## Bruk

`EntraIdTokenFetcher` brukes til å hente access tokens hos entra-id token issuer (azure). En må spesifisere hvilken app vekslet token er ment for i formatet `<cluster>.<namespace>.<appnavn>`.

Eksempel på tokenveksling for å nå en app i samme cluster og namespace:

```kotlin
fun getTokenForOtherApi(): String {
    val appName = "cluster.namespace.other-api"

    val tokenFetcher = EntraIdTokenFetcherBuilder.buildFetcher()
   
    return azureService.exchangeToken(appName)
}
```

## Bruk av biblioteket ved lokal kjøring 

Dette biblioteket forventer at følgende miljøvariabler er satt:

 - AZURE_APP_CLIENT_ID
 - AZURE_APP_TENANT_ID
 - AZURE_APP_JWK
 - AZURE_OPENID_CONFIG_ISSUER
 - AZURE_OPENID_CONFIG_TOKEN_ENDPOINT

Når nais-yaml er konfigurert riktig settes disse av plattformen ved kjøring i miljø. Ved lokal kjøring må disse også være satt.

Se [nais-dokumentasjonen](https://doc.nais.io/auth/entra-id/#runtime-variables-credentials) for forklaring på disse miljøvariablene.
