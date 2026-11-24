rootProject.name = "tms-ktor-token-support"
include("entra-id-token-verification")
include("entra-id-token-verification-mock")
include("entra-id-token-fetcher")
include("user-login-routes")
include("user-token-verification")
include("user-token-verification-mock")
include("user-token-exchange")


dependencyResolutionManagement {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        versionCatalogs {
            create("managed") {
                from("no.nav.tms:tms-dependency-admin:20261111.16.1")
            }
        }
    }
}