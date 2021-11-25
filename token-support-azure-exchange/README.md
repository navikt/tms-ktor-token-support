# token-support-azure-exchange

Dette biblioteket tilbyr en måte for en ktor app å hente azure access tokens ment for client-client kommunikasjon.


## Nais-yaml

Bruk av biblioteket forutsetter at nais-yaml er konfigurert for azure:

```yaml
spec:
  azure:
    application:
      enabled: true
```

## Oppsett

Biblioteket tilbyr ett interface `AzureService` med to implementasjoner, `CachingAzureService` og `NonCachingAzureService`.
Disse bygges ved hjelp av `AzureServiceBuilder`: 

```kotlin
fun Application.mainModule() {

    val serviceWithCache = AzureServiceBuilder.buildAzureService(
        cachingEnabled = true,
        maxCachedEntries = 100,
        cacheMarginSeconds = 10,
        enableDefaultProxy = true

    )
   
    val serviceWithoutCache = AzureServiceBuilder.buildAzureService(
        cachingEnabled = false,
    )
}
```

Default proxy er nødvendig dersom appen kjører on-prem med webproxy.

### Caching

Grunnet potensiell høy trafikk mot azure anbefales det å ikke skru av caching av access tokens i `AzureService`.

Default instillinger i `AzureServiceBuilder` er som følger: 
```kotlin
cachingEnabled = true
maxCachedEntries = 1000
cacheMarginSeconds = 5
```

`cacheMarginSeconds` bestemmer hvor lenge før access-tokenet egentlig utløper at vi invaliderer det fra cachen, for 
å kompensere for potensielle tregheter.   

Caching gjøres med clientnavn på målapp som nøkkel. 

## Bruk

`AzureService` brukes til å hente access tokens hos azure. En må spesifisere hvilken app vekslet token er ment for i formatet `<cluster>.<namespace>.<appnavn>`.

Eksempel på tokenveksling for å nå en app i samme cluster og namespace:

```kotlin
fun getTokenForOtherApi(subjectToken: String): String {
    val appName = "cluster.namespace.other-api"

    val azureService = AzureServiceBuilder.buildAzureService()
   
    return azureService.exchangeToken(subjectToken, appName)
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

Se [nais-dokumentasjonen](https://doc.nais.io/security/auth/azure-ad/index.html#runtime-variables-credentials) for forklaring på disse miljøvariablene.
