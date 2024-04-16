package no.nav.tms.token.support.azure.validation

// Proxy for System environment which allows for mocking or overwriting default env
object AzureEnvironment {
    private val baseEnv = System.getenv()

    private val env = mutableMapOf<String, String>()

    init {
        env.putAll(baseEnv)
    }

    fun get(name: String) = env[name]

    fun extend(envMap: Map<String, String>) {
        env.putAll(envMap)
    }

    fun reset() {
        env.clear()
        env.putAll(baseEnv)
    }
}
