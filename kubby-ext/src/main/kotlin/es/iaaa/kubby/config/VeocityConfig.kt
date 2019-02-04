package es.iaaa.kubby.config

import es.iaaa.kubby.ktor.features.VelocityConfiguration
import io.ktor.application.ApplicationEnvironment
import java.nio.charset.Charset

/**
 * Maps the configuration to a [VelocityConfiguration] instance.
 */
fun ApplicationEnvironment.toVelocityConfiguration() =
    VelocityConfiguration(
        classLoader = classLoader,
        resourceLoaderPath = config.property("kubby.velocity.resource-loader-path").getString(),
        suffix = config.property("kubby.velocity.suffix").getString(),
        charset = Charset.forName(config.property("kubby.velocity.charset").getString())
    )