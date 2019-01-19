package es.iaaa.kubby.app.pubby.rest.api

import es.iaaa.kubby.content.createResourceViewController
import io.ktor.routing.Route

/**
 * Registers a resource to [Map] transformation.
 */
fun Route.pageEntityController() = createResourceViewController {
    resource.toEntityDto(config, dataUri).toMap()
}