package es.iaaa.kubby

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.ForwardedHeaderSupport
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        commandLineEnvironment(args)
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
    installKubby()
}
