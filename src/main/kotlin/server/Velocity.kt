package es.iaaa.kubby.server

import io.ktor.application.Application
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository

fun VelocityEngine.setup() {
    templates {
        resource("404.vm")
        resource("header.vm")
        resource("footer.vm")
    }
}

fun VelocityEngine.templates(configure: VelocityEngine.() -> Unit) {
    setProperty("resource.loader", "string")
    addProperty("string.resource.loader.class", StringResourceLoader::class.java.name)
    addProperty("string.resource.loader.repository.static", "false")
    init() // need to call `init` before trying to retrieve string repository
    this.configure()// (configure)
}

fun VelocityEngine.resource(name: String) {
    val repository = getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT) as StringResourceRepository
    repository.putStringResource(name, Application::class.java.getResource("/templates/$name").readText())
}
