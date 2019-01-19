package es.iaaa.kubby.app.pubby.config

import es.iaaa.kubby.app.pubby.rest.api.pageEntityController
import es.iaaa.kubby.config.createKubbyModule
import es.iaaa.kubby.content.dataContent
import es.iaaa.kubby.content.indexContent
import es.iaaa.kubby.content.resourceContent
import es.iaaa.kubby.features.RDF
import es.iaaa.kubby.features.kubbyRiot
import es.iaaa.kubby.features.kubbySetup
import es.iaaa.kubby.metadata.KubbyMetadata
import es.iaaa.kubby.metadata.documentMetadata
import es.iaaa.kubby.metadata.provenanceMetadata
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
        kubbySetup(environment)
    }

    install(KubbyMetadata) {
        documentMetadata()
        provenanceMetadata()
    }

    install(ContentNegotiation) {
        kubbyRiot {
            contentTypes.add(RDF.TURTLE)
        }
    }

    install(Routing) {
        static("static") {
            resources("static")
        }
        indexContent()
        resourceContent()
        dataContent()
        pageEntityController()
    }
}
