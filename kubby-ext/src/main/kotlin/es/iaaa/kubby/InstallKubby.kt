package es.iaaa.kubby

import es.iaaa.kubby.config.module
import es.iaaa.kubby.config.velocityConfig
import es.iaaa.kubby.ktor.features.Metadata
import es.iaaa.kubby.ktor.features.RDF
import es.iaaa.kubby.ktor.features.metadata.document
import es.iaaa.kubby.ktor.features.metadata.provenance
import es.iaaa.kubby.ktor.features.rdf
import es.iaaa.kubby.ktor.features.setup
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
import org.koin.ktor.ext.installKoin
import org.koin.log.Logger
import org.koin.log.PrintLogger

/**
 * Install Kubby in an [Application]
 */
fun Application.installKubby(
    args: Array<String> = emptyArray(),
    list: List<Module> = emptyList(),
    properties: KoinProperties = KoinProperties(),
    logger: Logger = PrintLogger()
) {

    val appConfig = commandLineConfig(args)

    installKoin(listOf(module(appConfig)) + list, properties, logger)

    install(Velocity) {
        setup(velocityConfig(environment.classLoader, appConfig))
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
