package es.iaaa.kubby.ktor.features

import com.google.common.reflect.ClassPath.from
import io.ktor.application.ApplicationEnvironment
import io.ktor.config.ApplicationConfig
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository
import java.nio.charset.Charset

/**
 * Setups the [VelocityEngine].
 *
 * The [ApplicationEnvironment] is necessary not only for accessing to the Velocity configuration
 * but also to the class loader for locating the templates.
 */
fun VelocityEngine.setup(env: ApplicationEnvironment) {
    setProperty("resource.loader", "string")
    addProperty("string.resource.loader.class", StringResourceLoader::class.java.name)
    addProperty("string.resource.loader.repository.static", "false")
    init()

    val repository = getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT) as StringResourceRepository
    val conf = env.config.toVelocityConfiguration()
    from(env.classLoader).resources
        .filter {
            it.toString().run { startsWith(conf.resourceLoaderPath) && endsWith(conf.suffix) }
        }.forEach {
            repository.putStringResource(
                it.resourceName.substring(conf.resourceLoaderPath.length),
                it.asCharSource(conf.charset).read()
            )
        }
}

/**
 * Configuration class.
 */
data class VelocityConfiguration(
    val resourceLoaderPath: String,
    val suffix: String,
    val charset: Charset
)

/**
 * Maps the configuration to a [VelocityConfiguration] instance.
 */
fun ApplicationConfig.toVelocityConfiguration() =
    VelocityConfiguration(
        resourceLoaderPath = property("kubby.velocity.resource-loader-path").getString(),
        suffix = property("kubby.velocity.suffix").getString(),
        charset = Charset.forName(property("kubby.velocity.charset").getString())
    )