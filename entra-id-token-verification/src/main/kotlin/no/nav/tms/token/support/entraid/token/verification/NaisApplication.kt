package no.nav.tms.token.support.entraid.token.verification

import com.auth0.jwt.interfaces.DecodedJWT

data class NaisApplication(
    val cluster: String,
    val namespace: String,
    val app: String
) {
    override fun toString(): String {
        return "$cluster:$namespace:$app"
    }

    companion object {
        const val AZP_CLAIM_NAME = "azp_name"
        private val azpPattern = "([^:]+):([^:]+):([^:]+)".toRegex()

        internal fun fromClaims(decodedJWT: DecodedJWT): NaisApplication? {
            val azpClaim = decodedJWT.getClaim(AZP_CLAIM_NAME).asString()

            return if (azpClaim.isNullOrEmpty()) {
                null
            } else {
                azpPattern
                    .matchEntire(azpClaim)
                    ?.destructured?.let { (cluster, namespace, app) ->
                        NaisApplication(
                            cluster = cluster,
                            namespace = namespace,
                            app = app,
                        )
                    }
            }
        }
    }
}
