package es.iaaa.kubby.config

import es.iaaa.kubby.ktor.features.VelocityConfiguration
import io.ktor.application.ApplicationEnvironment
import java.nio.charset.Charset

/**
 * Maps the configuration to a [VelocityConfiguration] instance.
 */
fun ApplicationEnvironment.toVelocityConfiguration() =
    config.config("kubby.velocity").let {
        VelocityConfiguration(
            classLoader = classLoader,
            resourceLoaderPath = it.property("resource-loader-path").getString(),
            suffix = it.property("suffix").getString(),
            charset = Charset.forName(it.property("charset").getString())
        )
    }