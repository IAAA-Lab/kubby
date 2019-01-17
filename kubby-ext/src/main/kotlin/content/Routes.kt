package es.iaaa.kubby.content

import es.iaaa.kubby.config.*
import es.iaaa.kubby.description.DescriptionHandler
import es.iaaa.kubby.repository.DataSource
import es.iaaa.kubby.repository.NULL_NS_URI
import es.iaaa.kubby.repository.QName
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.ContentType.Text
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.RequestConnectionPoint
import io.ktor.request.contentType
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


private const val PATH_PARAMETER_NAME = "static-content-path-parameter"

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
        get("{$PATH_PARAMETER_NAME...}") {
            val relativePath = call.parameters.getAll(PATH_PARAMETER_NAME)?.joinToString(File.separator) ?: return@get
            val contentType = call.request.contentType().withoutParameters()
            if (Text.Html.match(contentType)) {
                call.respondSeeOther("${config.pagePath}/$relativePath")
            } else  {
                call.respondSeeOther("${config.dataPath}/$relativePath")
            }
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
        get("{$PATH_PARAMETER_NAME...}") {
            val relativePath = call.parameters.getAll(PATH_PARAMETER_NAME)?.joinToString(File.separator) ?: return@get
            val base = context.request.origin.buildBase("${config.dataPath}/$relativePath")
            val qname = QName("$base${config.resourcePath}/", relativePath)
            val model = dataSource.describe(qname)
            call.attributes.put(ContentKeys.timeId, GregorianCalendar.getInstance())
            call.attributes.put(ContentKeys.resourceId, qname.toString())
            call.attributes.put(ContentKeys.pageId, context.request.origin.uriRequest)
            call.attributes.put(ContentKeys.aboutId, "$base${config.aboutPath}")
            call.respond(model)
        }
    }
}

/**
 * Set up a routing tree to serve page content.
 */
fun Route.pageContent() {
    val config = application.environment.config
    val handler = DescriptionHandler(config)
    val dataSource by inject<DataSource>()
    route(config.pagePath) {
        get("{$PATH_PARAMETER_NAME...}") {
            val relativePath = call.parameters.getAll(PATH_PARAMETER_NAME)?.joinToString(File.separator) ?: return@get
            val base = context.request.origin.buildBase("${config.pagePath}/$relativePath")
            val qname = QName("$base${config.resourcePath}/", relativePath)
            val data = QName("$base${config.dataPath}/", relativePath)
            val model = dataSource.describe(qname)
            val content = handler.contentOf(model.getResource(qname.toString()), data.toString())
            call.respond(HttpStatusCode.OK, VelocityContent("page.vm", content))
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

fun RequestConnectionPoint.buildBase(path: String): String {
    val sb = StringBuilder()
    sb.append("$scheme://$host")
    if ((scheme == "http" && port != 80) ||
        (scheme == "https" && port != 443)) {
        sb.append(":$port")
    }
    val keep = uri.length - path.length
    sb.append(uri.subSequence(0, keep))
    return sb.toString()
}


val RequestConnectionPoint.uriRequest: String get() = "$scheme://$authority$uri"

val RequestConnectionPoint.authority: String
    get() {
        val sb = StringBuffer()
        sb.append(host)
        if ((scheme == "http" && port != 80) ||
            (scheme == "https" && port != 443)) {
            sb.append(":$port")
        }
        return sb.toString()
    }