package es.iaaa.kubby

import es.iaaa.kubby.config.KubbyConfig
import es.iaaa.kubby.datasource.DataSource
import es.iaaa.kubby.datasource.EmptyDataSource
import es.iaaa.kubby.features.riot
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.velocity.Velocity
import io.ktor.velocity.VelocityContent
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository
import org.koin.dsl.module.module
import org.koin.ktor.ext.inject
import org.koin.standalone.StandAloneContext.startKoin


val kubbyModule = module {
    single<DataSource> { EmptyDataSource() }
}


/**
 * Entry Point of the application.
 * This function is referenced in the resources/application.conf
 */
fun Application.main() {
    // Install Ktor features
    // This adds automatically Date and Server headers.
    install(DefaultHeaders)
    // This uses the logger to log every request/response
    install(CallLogging)
    // This install Velocity and configure the Velocity Engine
    install(Velocity) {
        // this: VelocityEngine
        templates {
            resource("404.vm")
            resource("header.vm")
            resource("footer.vm")
        }
    }

    // Lazy inject DataSource
    val dao: DataSource by inject()


    // Routing section
    // Register all the routes of the application
    routing {
        static("static") {
            resources("static")
        }
        resource()
        data(dao)
        page()
    }
}


fun VelocityEngine.templates(configure: VelocityEngine.() -> Unit) {
    setProperty("resource.loader", "string")
    addProperty("string.resource.loader.class", StringResourceLoader::class.java.name)
    addProperty("string.resource.loader.repository.static", "false")
    init() // need to call `init` before trying to retrieve string repository
    this.configure()// (configure)
}

fun VelocityEngine.resource(name: String) {
    val repository = getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT) as StringResourceRepository
    repository.putStringResource(name, Application::class.java.getResource("/templates/$name").readText())
}


fun Route.resource() {
    route(KubbyConfig.route.resource) {
        get("{id}") {
            call.respondRedirect("${KubbyConfig.route.data}/${call.parameters["id"]}")
        }
    }
}

fun Route.data(dao: DataSource) {
    route(KubbyConfig.route.data) {
        install(ContentNegotiation) {
            riot()
        }
        get("{id}") {
            val model = dao.describe("", call.parameters["id"]!!)
            call.respond(model)
        }
    }
}

fun Route.page() {
    route(KubbyConfig.route.page) {
        val model = mutableMapOf("a" to 1)
        get("{id}") {
            call.respond(HttpStatusCode.NotFound, VelocityContent("404.vm", model))
        }
    }
}

fun main(args: Array<String>) {
    // Start Koin
    startKoin(listOf(kubbyModule))
    // Start Ktor
    embeddedServer(
        Netty,
        port = 8080,
        module = Application::main
    ).start()
}