package no.nav.tms.token.support.user.login.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*


class UserLoginRoutesConfig {
    var enableDefaultProxy: Boolean = false
    var routesPrefix: String? = null
}

class UserLoginRoutes {

    companion object : BaseApplicationPlugin<Application, UserLoginRoutesConfig, UserLoginRoutes> {

        override val key: AttributeKey<UserLoginRoutes> = AttributeKey("UserLoginRoutes")

        override fun install(pipeline: Application, configure: UserLoginRoutesConfig.() -> Unit): UserLoginRoutes {

            val config = UserLoginRoutesConfig().also(configure)

            pipeline.routing {
                idPortenLoginApi(
                    tokenVerifier = initializeTokenVerifier(config.enableDefaultProxy, null),
                    rootpath = pipeline.rootPath,
                    routesPrefix = config.routesPrefix
                )
            }

            return UserLoginRoutes()
        }
    }
}

