package es.iaaa.kubby

import es.iaaa.kubby.config.module
import es.iaaa.kubby.server.setup
import io.ktor.application.Application
import io.ktor.application.install
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

    // Routing section
    // Register all the routes of the application
    install(Routing) {
        setup()
    }

}