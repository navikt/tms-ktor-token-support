package no.nav.tms.token.support.idporten.sidecar

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import no.nav.tms.token.support.idporten.sidecar.install.idPortenLoginApi
import no.nav.tms.token.support.idporten.sidecar.install.initializeTokenVerifier


class IdPortenLoginConfig {
    var enableDefaultProxy: Boolean = false
    var routesPrefix: String? = null
}

class IdPortenLogin {

    companion object : BaseApplicationPlugin<Application, IdPortenLoginConfig, IdPortenLogin> {

        override val key: AttributeKey<IdPortenLogin> = AttributeKey("IdPortenLogin")

        override fun install(pipeline: Application, configure: IdPortenLoginConfig.() -> Unit): IdPortenLogin {

            val config = IdPortenLoginConfig().also(configure)

            val hello = pipeline.environment.rootPath

            pipeline.routing {
                idPortenLoginApi(
                    tokenVerifier = initializeTokenVerifier(config.enableDefaultProxy, null),
                    rootpath = hello,
                    routesPrefix = config.routesPrefix
                )
            }

            return IdPortenLogin()
        }
    }
}

