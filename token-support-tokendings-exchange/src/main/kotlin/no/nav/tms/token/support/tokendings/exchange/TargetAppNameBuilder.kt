package no.nav.tms.token.support.tokendings.exchange

class TargetAppNameBuilder(private val cluster: String, private val namespace: String) {
    fun buildName(appName: String, cluster: String = this.cluster, namespace: String = this.namespace): String {
        return "$cluster:$namespace:$appName"
    }
}
