package es.iaaa.kubby.server

import es.iaaa.kubby.datasource.DataSource
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.ForwardedHeaderSupport
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.velocity.Velocity
import org.koin.ktor.ext.inject
import org.koin.standalone.StandAloneContext

fun startServer() {
    StandAloneContext.startKoin(listOf(module))
    embeddedServer(
        Netty,
        module = Application::main
    ).start(true)
}

fun Application.main() {
    // Install Ktor features
    // This adds automatically Date and Server headers.
    install(DefaultHeaders)
    // This uses the logger to log every request/response
    install(CallLogging)
    // This install support for forwarded headers
    install(ForwardedHeaderSupport)
    install(XForwardedHeaderSupport)
    // This install Velocity and configure the Velocity Engine
    install(Velocity) {
        setup()
    }

    // Lazy inject DataSource
    val dataSource: DataSource by inject()

    // Routing section
    // Register all the routes of the application
    install(Routing) {
        setup(dataSource)
    }
}







