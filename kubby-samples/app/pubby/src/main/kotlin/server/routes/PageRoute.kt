package es.iaaa.kubby.server.routes

import es.iaaa.kubby.config.Configuration
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.velocity.VelocityContent

fun Route.page() {
    route(Configuration.route.page) {
        val model = mutableMapOf("a" to 1)
        get("{id}") {
            call.respond(HttpStatusCode.NotFound, VelocityContent("404.vm", model))
        }
    }
}
