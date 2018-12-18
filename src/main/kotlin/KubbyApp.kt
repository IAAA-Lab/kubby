package es.iaaa.kubby

import com.github.jsonldjava.core.JsonLdOptions
import es.iaaa.kubby.sources.DataSource
import es.iaaa.kubby.sources.EmptyDataSource
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.velocity.Velocity
import io.ktor.velocity.VelocityContent
import org.apache.jena.query.DatasetFactory
import org.apache.jena.riot.JsonLDWriteContext
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.RDFFormat
import org.apache.jena.riot.system.RiotLib
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository
import java.io.StringWriter


val dao: DataSource = EmptyDataSource()

/**
 * Entry Point of the application.
 * This function is referenced in the resources/application.conf
 */
fun Application.main() {
    mainWithDependencies(dao)
}

/**
 * This function is called from the entry point and tests to configure an application.
 */
fun Application.mainWithDependencies(dao: DataSource) {
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
    route("/") {
        route("resource") {
            get("{id}") {
                call.respondRedirect("/data/${call.parameters["id"]}")
            }
        }
    }
}

fun Route.data(dao: DataSource) {
    route("/") {
        route("data") {
            get("{id}") {
                val model = dao.describe(call.parameters["id"]!!)
                val out = StringWriter()
                val opts = JsonLdOptions()
                opts.processingMode = JsonLdOptions.JSON_LD_1_1
                opts.explicit = true
                val ctx = JsonLDWriteContext()
                ctx.setOptions(opts)
                val g = DatasetFactory.wrap(model).asDatasetGraph()
                val w = RDFDataMgr.createDatasetWriter(RDFFormat.JSONLD_COMPACT_PRETTY)
                val pm = RiotLib.prefixMap(g)
                println(pm)
                w.write(out, g, pm, null, ctx)
                call.respondText(
                    text = out.toString(),
                    contentType = ContentType("application", "ld+json")
                )
            }
        }
    }
}

fun Route.page() {
    route("/") {
        route("page") {
            val model = mutableMapOf("a" to 1)
            get("{id}") {
                call.respond(HttpStatusCode.NotFound, VelocityContent("404.vm", model))
            }
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        port = 8080,
        module = Application::main
    ).start()
}