package es.iaaa.kubby.app.pubby

import es.iaaa.kubby.config.ProjectDescription
import es.iaaa.kubby.installKubby
import es.iaaa.kubby.rest.api.PageResponse
import es.iaaa.kubby.rest.api.page
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.routing
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.velocity.VelocityContent
import org.koin.ktor.ext.inject

lateinit var argsKubby: Array<String>

/**
 * Start embedded Netty with [args] and launch Ktor.
 */
fun main(args: Array<String>) {
    argsKubby = args
    embeddedServer(
        factory = Netty,
        environment = commandLineEnvironment(args),
        configure = {}
    ).start(wait = true)
}

/**
 * [Application] configuration.
 */
fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ForwardedHeaderSupport)
    install(XForwardedHeaderSupport)
    install(CORS) {
        anyHost()
    }

    installKubby(argsKubby)

    routing {
        static("static") {
            resources("static")
        }
        page {
            val projectDesc by inject<ProjectDescription>()
            val content = resource.toEntityDto(projectDesc, dataUri).toMap()
            if (!resource.model.isEmpty)
                PageResponse(HttpStatusCode.OK, VelocityContent("page.vm", content))
            else
                PageResponse(HttpStatusCode.NotFound, VelocityContent("404.vm", content))
        }
    }
}


