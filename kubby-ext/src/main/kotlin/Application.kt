package es.iaaa.kubby

import es.iaaa.kubby.config.module
import es.iaaa.kubby.content.dataContent
import es.iaaa.kubby.content.indexContent
import es.iaaa.kubby.content.pageContent
import es.iaaa.kubby.content.resourceContent
import es.iaaa.kubby.features.RDF
import es.iaaa.kubby.features.riot
import es.iaaa.kubby.metadata.Metadata
import es.iaaa.kubby.metadata.documentMetadata
import es.iaaa.kubby.metadata.provenanceMetadata
import es.iaaa.kubby.server.setup
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.ContentNegotiation
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.Routing
import io.ktor.velocity.Velocity
import org.koin.ktor.ext.installKoin

/**
 * Ktor Kubby extensions.
 *
 * @author Francisco J Lopez-Pellicer
 */


/**
 * Help configure Kubby for Ktor.
 */
fun Application.installKubby() {
    installKoin(listOf(module))

    // TODO installKubby()
    // TODO that creates the module factory ( moduleFactory(environment.config) )
    // TODO that installs Kubby Routing
    // TODO that install velocity

    // This install Velocity and configure the Velocity Engine
    install(Velocity) {
        setup()
    }

    install(Metadata) {
        documentMetadata()
        provenanceMetadata()
    }
    install(ContentNegotiation) {
        riot {
            contentTypes.add(RDF.TURTLE)
        }
    }

    // Routing section
    // Register all the routes of the application
    install(Routing) {
        trace { application.log.trace(it.buildText()) }
        static("static") {
            resources("static")
        }

        indexContent()
        resourceContent()
        dataContent()
        pageContent()
    }

}