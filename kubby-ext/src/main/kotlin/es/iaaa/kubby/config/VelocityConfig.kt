package es.iaaa.kubby.config

import com.typesafe.config.Config
import es.iaaa.kubby.ktor.features.VelocityConfiguration
import java.nio.charset.Charset

/**
 * Maps the configuration to a [VelocityConfiguration] instance.
 */
fun velocityConfig(classLoader: ClassLoader, config: Config) =
    config.getConfig("kubby.velocity").let {
        VelocityConfiguration(
            classLoader = classLoader,
            resourceLoaderPath = it.getString("resource-loader-path"),
            suffix = it.getString("suffix"),
            charset = Charset.forName(it.getString("charset"))
        )
    }