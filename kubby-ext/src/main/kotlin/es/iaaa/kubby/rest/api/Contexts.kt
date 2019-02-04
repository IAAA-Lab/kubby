package es.iaaa.kubby.rest.api

import es.iaaa.kubby.services.DescribeEntityService
import io.ktor.application.ApplicationCall
import io.ktor.features.origin
import io.ktor.http.ContentType
import io.ktor.http.RequestConnectionPoint
import io.ktor.http.Url
import io.ktor.http.toURI
import io.ktor.request.acceptItems
import org.apache.jena.rdf.model.Resource
import java.util.*


/**
 * Root of the context classes.
 */
sealed class Context

/**
 * Interface of the context
 */

interface ContentContext {
    val resource: Resource
    val page: String
    val data: String
    val time: Calendar
}

/**
 * Context of the page request.
 */
data class PageContentContext(
    override val resource: Resource,
    override val page: String,
    override val data: String,
    override val time: Calendar = GregorianCalendar.getInstance()
) : Context(), ContentContext


/**
 * Context of the data request.
 */
data class DataContentContext(
    override val resource: Resource,
    override val page: String,
    override val data: String,
    override val time: Calendar = GregorianCalendar.getInstance()
) : Context(), ContentContext

/**
 * Context of the redirect request.
 */
data class RedirectContext(
    val page: String,
    val data: String
) : Context()

/**
 * Context of a failed request.
 */
object NoContext : Context()

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
 * Process page requests.
 */
internal fun ApplicationCall.processPageRequests(
    paramName: String,
    routes: Routes,
    service: DescribeEntityService
): Context {
    val (page, data, namespace, localId) = extractEntityUris(paramName, routes.pagePath, routes)
    return if (localId.isNotEmpty())
        PageContentContext(
            resource = service.findOne(namespace, localId),
            page = page,
            data = data
        )
    else NoContext
}

/**
 * Process data requests.
 */
internal fun ApplicationCall.processDataRequests(
    paramName: String,
    routes: Routes,
    service: DescribeEntityService
): Context {
    val (page, data, namespace, localId) = extractEntityUris(paramName, routes.dataPath, routes)
    return if (localId.isNotEmpty())
        DataContentContext(
            resource = service.findOne(namespace, localId),
            page = page,
            data = data
        )
    else NoContext
}

/**
 *  Process in redirects.
 */
internal fun ApplicationCall.processRedirects(
    paramName: String,
    routes: Routes,
    alternativeId: String = ""
): Context {
    val (page, data, _, effectiveId) = extractEntityUris(
        paramName,
        if (alternativeId.isNotEmpty()) "" else routes.resourcePath,
        routes
    )
    return if (effectiveId.isNotEmpty() xor alternativeId.isNotEmpty())
        RedirectContext(
            page = "$page$alternativeId",
            data = "$data$alternativeId"
        )
    else NoContext
}

/**
 * Extract paths.
 */
internal fun ApplicationCall.extractEntityUris(
    paramName: String,
    path: String,
    routes: Routes
): EntityUris {
    val localId = extractLocalPath(paramName)
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
internal fun ApplicationCall.extractLocalPath(paramName: String) = parameters
    .getAll(paramName)
    ?.joinToString("/")
    ?.replace("?", "%3F")
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
internal fun ApplicationCall.extractAcceptedTypes() = request.acceptItems().map { ContentType.parse(it.value) }

/**
 * Request authority.
 */
internal val RequestConnectionPoint.authority: String
    get() = "$host$lexicalPort"

/**
 * Request port.
 */

internal val RequestConnectionPoint.lexicalPort: String
    get() = if ((scheme == "http" && port != 80) || (scheme == "https" && port != 443)) ":$port" else ""