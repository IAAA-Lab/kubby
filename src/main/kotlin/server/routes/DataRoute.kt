package es.iaaa.kubby.server.routes

import com.typesafe.config.Config
import es.iaaa.kubby.config.Configuration
import es.iaaa.kubby.datasource.DataSource
import es.iaaa.kubby.features.RDF
import es.iaaa.kubby.features.riot
import es.iaaa.kubby.server.metadata.Metadata
import es.iaaa.kubby.server.metadata.documentMetadata
import es.iaaa.kubby.server.metadata.provenanceMetadata
import es.iaaa.kubby.util.AttributeKeys.aboutId
import es.iaaa.kubby.util.AttributeKeys.pageId
import es.iaaa.kubby.util.AttributeKeys.resourceId
import es.iaaa.kubby.util.AttributeKeys.timeId
import es.iaaa.kubby.util.buildBase
import es.iaaa.kubby.util.buildRequest
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.origin
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import java.util.*


fun Route.data(dao: DataSource) {

    val config: Config = Configuration.config.getConfig("kubby.route")
    val dataPath: String = config.getString("data")
    val resourcePath: String = config.getString("resource")
    val aboutPath: String = config.getString("about")

    route(dataPath) {
        install(Metadata) {
            documentMetadata()
            provenanceMetadata()
        }
        install(ContentNegotiation) {
            riot {
                contentTypes.add(RDF.TURTLE)
            }
        }
        get("{id}") {
            val id = context.parameters["id"]!!
            val base = context.request.origin.buildBase("$dataPath/$id")
            val ns = "$base$resourcePath/"
            val about = "$base$aboutPath"
            val model = dao.describe(ns, id)
            call.attributes.put(timeId, GregorianCalendar.getInstance())
            call.attributes.put(resourceId, ns + id)
            call.attributes.put(pageId, context.request.origin.buildRequest())
            call.attributes.put(aboutId, about)
            call.respond(model)
        }
    }
}
