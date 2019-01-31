package es.iaaa.kubby.app.pubby.config

import es.iaaa.kubby.app.pubby.rest.api.pageEntityController
import es.iaaa.kubby.config.createKubbyModule
import es.iaaa.kubby.ktor.features.Metadata
import es.iaaa.kubby.ktor.features.RDF
import es.iaaa.kubby.ktor.features.metadata.document
import es.iaaa.kubby.ktor.features.metadata.provenance
import es.iaaa.kubby.ktor.features.rdf
import es.iaaa.kubby.ktor.features.setup
import es.iaaa.kubby.rest.api.dataController
import es.iaaa.kubby.rest.api.indexController
import es.iaaa.kubby.rest.api.resourceController
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.Routing
import io.ktor.velocity.Velocity
import org.koin.ktor.ext.installKoin


/**
 * [Application] configuration.
 */
fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ForwardedHeaderSupport)
    install(XForwardedHeaderSupport)

    installKoin(listOf(createKubbyModule(environment.config)))

    install(Velocity) {
        setup(environment)
    }

    install(Metadata) {
        document()
        provenance()
    }

    install(ContentNegotiation) {
        rdf {
            contentTypes.add(RDF.TURTLE)
        }
    }

    install(Routing) {
        static("static") {
            resources("static")
        }
        indexController()
        resourceController()
        dataController()
        pageEntityController()
    }
}
