package es.iaaa.kubby.server.routes

import es.iaaa.kubby.config.Configuration
import io.ktor.application.call
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.resource() {
    route(Configuration.route.resource) {
        get("{id}") {
            call.respondRedirect("${Configuration.route.data}/${call.parameters["id"]}")
        }
    }
}
