package es.iaaa.kubby

import es.iaaa.kubby.config.KubbyConfig
import es.iaaa.kubby.datasource.*
import es.iaaa.kubby.features.riot
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.RequestConnectionPoint
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
import java.nio.file.Path
import java.nio.file.Paths

val target: Path = Paths.get("build/datasource/tdb2")
val data: Path = Paths.get("src/test/resources/Tetris.n3")


val kubbyModule = module {
    single<DataSource> {
        val config = Tdb2DataSourceConfiguration(
            path = target,
            definition = DatasourceDefinition.CREATE,
            data = data
        )
        val ds = Tdb2DataSource(config)
        RewriterDataSource(ds, "http://dbpedia.org/resource/")
    }
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
    // This install support for forwarded headers
    install(ForwardedHeaderSupport)
    install(XForwardedHeaderSupport)
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
            val id = context.parameters["id"]!!
            val ns = context.request.origin.buildResourceNamespace(KubbyConfig.route.data, id)
            val model = dao.describe(ns, id)
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

fun RequestConnectionPoint.buildResourceNamespace(route: String, id: String): String {
    val sb = StringBuilder()
    sb.append("$scheme://$host")
    when (scheme) {
        "http" -> if (port != 80) sb.append(":$port")
        "https" -> if (port != 443) sb.append(":$port")
        else -> {
        }
    }
    val keep = uri.length - route.length - id.length - 1
    sb.append(uri.subSequence(0, keep))
    sb.append(KubbyConfig.route.resource)
    sb.append("/")
    return sb.toString()
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