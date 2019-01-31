package es.iaaa.kubby.app.pubby.rest.api

import es.iaaa.kubby.config.ProjectDescription
import es.iaaa.kubby.rest.api.PageResponse
import es.iaaa.kubby.rest.api.pageController
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Route
import io.ktor.velocity.VelocityContent
import org.koin.ktor.ext.inject

/**
 * Registers a resource to [Map] transformation.
 */
fun Route.pageEntityController() {
    val projectDesc by inject<ProjectDescription>()
    return pageController {
        val (status, page) = if (!resource.model.isEmpty) {
            Pair(HttpStatusCode.OK, "page.vm")
        } else {
            Pair(HttpStatusCode.NotFound, "404.vm")
        }
        PageResponse(
            status = status,
            content = VelocityContent(page, resource.toEntityDto(projectDesc, data).toMap())
        )
    }
}
