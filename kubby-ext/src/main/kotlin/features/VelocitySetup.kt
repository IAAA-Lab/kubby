package es.iaaa.kubby.features

import com.google.common.reflect.ClassPath
import es.iaaa.kubby.config.velocityConfiguration
import io.ktor.application.ApplicationEnvironment
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository


fun VelocityEngine.setup(env: ApplicationEnvironment) {
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