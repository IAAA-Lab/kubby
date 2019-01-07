package es.iaaa.kubby.content

import es.iaaa.kubby.config.*
import es.iaaa.kubby.repository.DataSource
import es.iaaa.kubby.repository.NULL_NS_URI
import es.iaaa.kubby.repository.QName
import es.iaaa.kubby.util.AttributeKeys
import es.iaaa.kubby.util.buildBase
import es.iaaa.kubby.util.buildRequest
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import io.ktor.velocity.VelocityContent
import org.apache.jena.rdf.model.Model
import org.koin.ktor.ext.inject
import java.io.File
import java.util.*


private const val pathParameterName = "static-content-path-parameter"

/**
 * Set up a routing tree to handle the index resource.
 */
fun Route.indexContent() {
    val config = application.environment.config
    val dataSource by inject<DataSource>()
    get {
        config.indexResource?.let { index ->
            val qname = dataSource.qname(index)
            if (qname.namespaceURI != NULL_NS_URI) {
                call.respondSeeOther("${config.pagePath}/${qname.localPart}")
            }
        }
    }
}

/**
 * Set up a routing tree to redirect resource content.
 */
fun Route.resourceContent() {
    val config = application.environment.config
    route(config.resourcePath) {
        get("{$pathParameterName...}") {
            val relativePath = call.parameters.getAll(pathParameterName)?.joinToString(File.separator) ?: return@get
            call.respondSeeOther("${config.dataPath}/$relativePath")
        }
    }
}

/**
 * Set up a routing tree to serve data content.
 */
fun Route.dataContent() {
    val config = application.environment.config
    val dataSource by inject<DataSource>()
    route(config.dataPath) {
        get("{$pathParameterName...}") {
            val relativePath = call.parameters.getAll(pathParameterName)?.joinToString(File.separator) ?: return@get
            val base = context.request.origin.buildBase("${config.dataPath}/$relativePath")
            val qname = QName("$base${config.resourcePath}/", relativePath)
            val model = dataSource.describe(qname)
            call.attributes.put(AttributeKeys.timeId, GregorianCalendar.getInstance())
            call.attributes.put(AttributeKeys.resourceId, qname.toString())
            call.attributes.put(AttributeKeys.pageId, context.request.origin.buildRequest())
            call.attributes.put(AttributeKeys.aboutId, "$base${config.aboutPath}")
            call.respond(model)
        }
    }
}

/**
 * Set up a routing tree to serve page content.
 */
fun Route.pageContent() {
    val config = application.environment.config
    val dataSource by inject<DataSource>()
    route(config.pagePath) {
        get("{$pathParameterName...}") {
            val relativePath = call.parameters.getAll(pathParameterName)?.joinToString(File.separator) ?: return@get
            val base = context.request.origin.buildBase("${config.pagePath}/$relativePath")
            val qname = QName("$base${config.resourcePath}/", relativePath)
            val model = dataSource.describe(qname)
            processPageContentResponse(model)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.processPageContentResponse(
    m: Model
) {
    val model = mutableMapOf("a" to 1)
    call.respond(HttpStatusCode.NotFound, VelocityContent("404.vm", model))
}

/**
 * Responds to a client with a `301 Moved Permanently` or `302 Found` redirect
 */
suspend fun ApplicationCall.respondSeeOther(url: String) {
    response.headers.append(HttpHeaders.Location, url)
    respond(HttpStatusCode.SeeOther)
}
