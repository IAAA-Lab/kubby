package es.iaaa.kubby.rest.api

import es.iaaa.kubby.services.DescribeEntityService
import es.iaaa.kubby.services.IndexService
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
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.AttributeKey
import org.apache.jena.rdf.model.Resource
import org.koin.ktor.ext.inject
import java.util.*

/**
 * Context access key.
 */
val requestContextKey = AttributeKey<RequestContext>("kubby.requestContext")

/**
 * Routes configuration.
 */
data class Routes(
    val pagePath: String,
    val dataPath: String,
    val resourcePath: String
)

/**
 * Logic in [pageController] for populating the response from the [RequestContext].
 */
typealias PageAdapter = RequestContext.() -> PageResponse

/**
 * A response body ([content]) with [status].
 */
data class PageResponse(
    val status: HttpStatusCode,
    val content: Any
)

/**
 * Context of the page request.
 */
data class RequestContext(
    val resource: Resource,
    val page: String,
    val data: String,
    val time: Calendar = GregorianCalendar.getInstance()
)

/**
 * Sets up a route to handle the index resource if defined.
 */
fun Route.indexController() {
    val service by inject<IndexService>()
    val routes by inject<Routes>()
    service.indexLocalPart()?.let { localPart ->
        get {
            call.response.headers.append(
                HttpHeaders.Location,
                "${call.extractHierPart("/")}${routes.pagePath}/$localPart"
            )
            call.respond(HttpStatusCode.SeeOther)
        }
    }
}

/**
 * Sets up a routing tree to redirect resource content.
 */
fun Route.resourceController() {
    val routes by inject<Routes>()
    route(routes.resourcePath) {
        get("{$PATH_LOCAL_PART...}") {
            val localPart = call.extractLocalPath()
            val hierPart = call.extractHierPart("${routes.resourcePath}/$localPart")
            val url = if (Text.Html.match(call.extractContentType())) {
                "$hierPart${routes.pagePath}/$localPart"
            } else {
                "$hierPart${routes.dataPath}/$localPart"
            }
            call.response.headers.append(HttpHeaders.Location, url)
            call.respond(HttpStatusCode.SeeOther)
        }
    }
}

/**
 * Sets up a routing tree to serve data content.
 */
fun Route.dataController() {
    val service by inject<DescribeEntityService>()
    val routes by inject<Routes>()
    route(routes.dataPath) {
        get("{$PATH_LOCAL_PART...}") {
            call.process(routes.dataPath, routes, service).resource.apply {
                if (!model.isEmpty) call.respond(model)
            }
        }
    }
}

/**
 * Sets up a routing tree to serve page content.
 */
fun Route.pageController(adapt: PageAdapter) {
    val service by inject<DescribeEntityService>()
    val routes by inject<Routes>()
    route(routes.pagePath) {
        get("{$PATH_LOCAL_PART...}") {
            adapt(call.process(routes.pagePath, routes, service))
                .apply {
                    call.respond(status, content)
                }
        }
    }
}

/**
 * Common process in page and data controllers.
 */
private fun ApplicationCall.process(
    path: String,
    routes: Routes,
    service: DescribeEntityService
): RequestContext {
    val localPart = extractLocalPath()
    val hierPart = extractHierPart("$path/$localPart")
    val resource = service.findOne("$hierPart${routes.resourcePath}/", localPart)
    val context = RequestContext(
        resource = resource,
        page = "$hierPart${routes.pagePath}/$localPart",
        data = "$hierPart${routes.dataPath}/$localPart"
    )
    attributes.put(requestContextKey, context)
    return context
}

/**
 * Extracts the local part of the request.
 */
private fun ApplicationCall.extractLocalPath() = parameters
    .getAll(PATH_LOCAL_PART)
    ?.joinToString("/")
    ?: ""

/**
 * Extracts the hier part of the request by removing the [localPart].
 */
internal fun ApplicationCall.extractHierPart(localPart: String) =
    with(request.origin) {
        val sb = StringBuilder()
        sb.append("$scheme://$authority")
        if (uri.endsWith(localPart)) {
            sb.append(uri.subSequence(0, uri.length - localPart.length))
        } else {
            sb.append(uri)
        }
        sb.toString()
    }

/**
 * Extracts the content type of the request.
 */
private fun ApplicationCall.extractContentType() = request
    .contentType().withoutParameters()

/**
 * Authority.
 */
internal val RequestConnectionPoint.authority: String
    get() = StringBuffer().apply {
        append(host)
        if ((scheme == "http" && port != 80) ||
            (scheme == "https" && port != 443)
        ) {
            append(":$port")
        }
    }.toString()


/**
 * Identifier of the local part in route mode.
 */
private const val PATH_LOCAL_PART = "static-content-path-parameter"
