package es.iaaa.kubby

import es.iaaa.kubby.config.createKubbyModule
import es.iaaa.kubby.ktor.features.*
import es.iaaa.kubby.ktor.features.metadata.document
import es.iaaa.kubby.ktor.features.metadata.provenance
import es.iaaa.kubby.rest.api.data
import es.iaaa.kubby.rest.api.index
import es.iaaa.kubby.rest.api.resource
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.routing.routing
import io.ktor.velocity.Velocity
import org.koin.core.KoinProperties
import org.koin.dsl.module.Module
import org.koin.ktor.ext.inject
import org.koin.ktor.ext.installKoin
import org.koin.log.Logger
import org.koin.log.PrintLogger

/**
 * Install Kubby in an [Application]
 */
fun Application.installKubby(
    list: List<Module> = emptyList(),
    properties: KoinProperties = KoinProperties(),
    logger: Logger = PrintLogger()
) {
    installKoin(listOf(createKubbyModule(environment)) + list, properties, logger)

    install(Velocity) {
        val config by inject<VelocityConfiguration>()
        setup(config)
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

    routing {
        index()
        resource()
        data()
    }
}
