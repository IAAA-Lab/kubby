package es.iaaa.kubby.server.routes

import es.iaaa.kubby.config.Configuration
import es.iaaa.kubby.datasource.DataSource
import es.iaaa.kubby.features.RDF
import es.iaaa.kubby.features.riot
import es.iaaa.kubby.server.metadata.Metadata
import es.iaaa.kubby.server.metadata.documentMetadata
import es.iaaa.kubby.util.AttributeKeys.pageId
import es.iaaa.kubby.util.AttributeKeys.resourceId
import es.iaaa.kubby.util.buildRequest
import es.iaaa.kubby.util.buildResourceNamespace
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.origin
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route


fun Route.data(dao: DataSource) {
    route(Configuration.route.data) {
        install(Metadata) {
            documentMetadata()
        }
        install(ContentNegotiation) {
            riot {
                contentTypes.add(RDF.TURTLE)
            }
        }
        get("{id}") {
            val id = context.parameters["id"]!!
            val ns = context.request.origin.buildResourceNamespace(Configuration.route.data, id)
            val model = dao.describe(ns, id)
            call.attributes.put(resourceId, ns + id)
            call.attributes.put(pageId, context.request.origin.buildRequest())
            call.respond(model)
        }
    }
}
