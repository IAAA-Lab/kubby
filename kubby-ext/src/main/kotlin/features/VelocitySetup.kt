package es.iaaa.kubby.features

import com.google.common.reflect.ClassPath
import io.ktor.application.ApplicationEnvironment
import io.ktor.config.ApplicationConfig
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository
import java.nio.charset.Charset


fun VelocityEngine.kubbySetup(env: ApplicationEnvironment) {
    setProperty("resource.loader", "string")
    addProperty("string.resource.loader.class", StringResourceLoader::class.java.name)
    addProperty("string.resource.loader.repository.static", "false")
    init() // need to call `init` before trying to retrieve string repository

    val repository = getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT) as StringResourceRepository
    val conf = env.config.velocityConfiguration
    ClassPath.from(env.classLoader).resources.filter {
        val name = it.toString()
        name.startsWith(conf.resourceLoaderPath) && name.endsWith(conf.suffix)
    }.forEach {
        val name = it.resourceName.substring(conf.resourceLoaderPath.length)
        repository.putStringResource(name, it.asCharSource(conf.charset).read())
    }
}

data class VelocityConfiguration(
    val resourceLoaderPath: String,
    val suffix: String,
    val charset: Charset
)

// TODO test
val ApplicationConfig.velocityConfiguration: VelocityConfiguration
    get() = VelocityConfiguration(
        resourceLoaderPath = property("kubby.velocity.resource-loader-path").getString(),
        suffix = property("kubby.velocity.suffix").getString(),
        charset = Charset.forName(property("kubby.velocity.charset").getString())
    )