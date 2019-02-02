package es.iaaa.kubby.rest.api

import es.iaaa.kubby.services.DescribeEntityService
import es.iaaa.kubby.services.IndexService
import io.ktor.application.call
import io.ktor.http.ContentType.Text
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.AttributeKey
import org.koin.ktor.ext.inject

/**
 * Context access key.
 */
val requestContextKey = AttributeKey<ContentContext>("kubby.requestContext")

/**
 * Routes configuration.
 */
data class Routes(
    val pagePath: String,
    val dataPath: String,
    val resourcePath: String
)

/**
 * Logic in [pageController] for populating the response from the [ContentContext].
 */
typealias PageAdapter = ContentContext.() -> PageResponse

/**
 * A response body ([content]) with [status].
 */
data class PageResponse(
    val status: HttpStatusCode,
    val content: Any
)


/**
 * Sets up a route to handle the index resource if defined.
 */
fun Route.indexController() {
    val service by inject<IndexService>()
    val routes by inject<Routes>()
    get {
        service.indexLocalPart()?.let { localPart ->
            val ctx = call.processRedirects(PATH_LOCAL_PART, routes, localPart)
            if (ctx is RedirectContext) {
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
            val ctx = call.processRedirects(PATH_LOCAL_PART, routes)
            if (ctx is RedirectContext) {
                val requestPage = call.extractAcceptedTypes().any { Text.Html.match(it) }
                val url = if (requestPage) {
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
            val ctx = call.processRequests(PATH_LOCAL_PART, routes.dataPath, routes, service)
            if (ctx is ContentContext) {
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
            val ctx = call.processRequests(PATH_LOCAL_PART, routes.pagePath, routes, service)
            if (ctx is ContentContext) {
                call.attributes.put(requestContextKey, ctx)
                adapt(ctx).apply { call.respond(status, content) }
            }
        }
    }
}


/**
 * Identifier of the local part in route mode.
 */
internal const val PATH_LOCAL_PART = "static-content-path-parameter"
