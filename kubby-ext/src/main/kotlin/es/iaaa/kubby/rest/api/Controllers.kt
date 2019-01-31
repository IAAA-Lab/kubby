package es.iaaa.kubby.rest.api

import es.iaaa.kubby.services.DescribeEntityService
import es.iaaa.kubby.services.IndexService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.*
import io.ktor.http.ContentType.Text
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
 * Uris related to an entity
 */
data class EntityUris(
    val page: String,
    val data: String,
    val namespace: String,
    val localId: String
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
    val findable: Boolean,
    val time: Calendar = GregorianCalendar.getInstance()
)

/**
 * Context of the page redirect.
 */
data class RedirectContext(
    val page: String,
    val data: String,
    val findable: Boolean
)

/**
 * Sets up a route to handle the index resource if defined.
 */
fun Route.indexController() {
    val service by inject<IndexService>()
    val routes by inject<Routes>()
    service.indexLocalPart()?.let { localPart ->
        get {
            val ctx = call.processRedirects(routes, localPart)
            if (ctx.findable) {
                call.response.headers.append(HttpHeaders.Location, ctx.page)
                call.respond(HttpStatusCode.SeeOther)
            }
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
            val ctx = call.processRedirects(routes)
            if (ctx.findable) {
                val url = if (Text.Html.match(call.extractContentType())) {
                    ctx.page
                } else {
                    ctx.data
                }
                call.response.headers.append(HttpHeaders.Location, url)
                call.respond(HttpStatusCode.SeeOther)
            }
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
            val ctx = call.processRequests(routes.dataPath, routes, service)
            if (ctx.findable) {
                call.attributes.put(requestContextKey, ctx)
                ctx.resource.apply { if (!model.isEmpty) call.respond(model) }
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
            val ctx = call.processRequests(routes.pagePath, routes, service)
            if (ctx.findable) {
                call.attributes.put(requestContextKey, ctx)
                adapt(ctx).apply { call.respond(status, content) }
            }
        }
    }
}

/**
 * Common processRedirects in page and data controllers.
 */
private fun ApplicationCall.processRequests(
    path: String,
    routes: Routes,
    service: DescribeEntityService
): RequestContext {
    val (page, data, namespace, localId) = extractEntityUris(path, routes)
    return RequestContext(
        resource = service.findOne(namespace, localId),
        page = page,
        data = data,
        findable = localId.isNotEmpty()
    )
}

/**
 *  Process in redirects.
 */
internal fun ApplicationCall.processRedirects(
    routes: Routes,
    alternativeId: String = ""
): RedirectContext {
    val (page, data, _, effectiveId) = extractEntityUris(if (alternativeId.isNotEmpty()) "" else routes.resourcePath, routes)
    return RedirectContext(
        page = "$page$alternativeId",
        data = "$data$alternativeId",
        findable = effectiveId.isNotEmpty() xor alternativeId.isNotEmpty()
    )
}

/**
 * Extract paths.
 */
internal fun ApplicationCall.extractEntityUris(
    path: String,
    routes: Routes
): EntityUris {
    val localId = extractLocalPath()
    val base = extractHierPart("$path/$localId").let {
        if (it.endsWith(path)) it.dropLast(path.length) else it
    }
    return EntityUris(
        page = "$base${routes.pagePath}/$localId".toNormalizedUrlString(),
        data = "$base${routes.dataPath}/$localId".toNormalizedUrlString(),
        namespace = "$base${routes.resourcePath}/".toNormalizedUrlString(),
        localId = localId
    )
}

internal fun String.toNormalizedUrlString() = Url(this).toURI().normalize().toString()


/**
 * Extracts the local part of the request.
 */
internal fun ApplicationCall.extractLocalPath() = parameters
    .getAll(PATH_LOCAL_PART)
    ?.joinToString("/")
    ?.replace("?","%3F")
    ?: ""

/**
 * Extracts the hier part of the request by removing the [localPart].
 */
internal fun ApplicationCall.extractHierPart(localPart: String) =
    with(request.origin) {
        val sb = StringBuilder()
        sb.append("$scheme://$authority")
        if (uri.endsWith(localPart)) {
            sb.append(uri.dropLast(localPart.length))
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
internal const val PATH_LOCAL_PART = "static-content-path-parameter"
