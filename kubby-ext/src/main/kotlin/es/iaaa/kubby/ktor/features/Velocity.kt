package es.iaaa.kubby.ktor.features

import com.google.common.reflect.ClassPath.from
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository
import java.nio.charset.Charset

/**
 * Setups the [VelocityEngine].
 *
 */
fun VelocityEngine.setup(conf: VelocityConfiguration) {
    setProperty("resource.loader", "string")
    addProperty("string.resource.loader.class", StringResourceLoader::class.java.name)
    addProperty("string.resource.loader.repository.static", "false")
    init()

    val repository = getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT) as StringResourceRepository
    from(conf.classLoader).resources
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
    val classLoader: ClassLoader,
    val resourceLoaderPath: String,
    val suffix: String,
    val charset: Charset
)
