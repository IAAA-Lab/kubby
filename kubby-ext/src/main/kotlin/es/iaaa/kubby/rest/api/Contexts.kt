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
 * Context of the page or data request.
 */
data class ContentContext(
    val resource: Resource,
    val page: String,
    val data: String,
    val time: Calendar = GregorianCalendar.getInstance()
) : Context()

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
 * Common processRedirects in page and data controllers.
 */
internal fun ApplicationCall.processRequests(
    paramName: String,
    path: String,
    routes: Routes,
    service: DescribeEntityService
): Context {
    val (page, data, namespace, localId) = extractEntityUris(paramName, path, routes)
    return if (localId.isNotEmpty())
        ContentContext(
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
