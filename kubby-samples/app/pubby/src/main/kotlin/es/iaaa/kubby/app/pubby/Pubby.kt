package es.iaaa.kubby.app.pubby

import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

/**
 * Start embedded Netty with [args] and launch Ktor.
 */
fun main(args: Array<String>) {
    embeddedServer(
        factory = Netty,
        environment = commandLineEnvironment(args),
        configure = {}
    ).start(wait = true)
}
