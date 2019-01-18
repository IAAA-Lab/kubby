package es.iaaa.kubby.config

import io.ktor.config.ApplicationConfig
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property


const val ROOT_KEY = "kubby"


/**
 * Get the project name.
 */
val ApplicationConfig.projectName: String
    get() = property("$ROOT_KEY.project-name").getString()

/**
 * Get the project homepage.
 */
val ApplicationConfig.projectHomepage: String
    get() = property("$ROOT_KEY.project-homepage").getString()

/**
 * Get the project used prefixes.
 *
 * TODO Ensure that these prefixes are added to the model after retrieval
 *
 */
val ApplicationConfig.usePrefixes: Map<String, String>
    get() = configList("$ROOT_KEY.use-prefixes").map {
        val prefix = it.property("prefix").getString()
        val uri = it.property("uri").getString()
        prefix to uri
    }.toMap()

/**
 * Get the default language of the project.
 */
val ApplicationConfig.defaultLanguage: String
    get() = property("$ROOT_KEY.default-language").getString()

/**
 * Get the index resource of the project.
 */
val ApplicationConfig.indexResource: String?
    get() = propertyOrNull("$ROOT_KEY.index-resource")?.getString()

/**
 * Get the list of label properties.
 */
val ApplicationConfig.labelProperties: List<Property>
    get() = getProperties("$ROOT_KEY.label-properties")

/**
 * Get the list of comment properties.
 */
val ApplicationConfig.commentProperties: List<Property>
    get() = getProperties("$ROOT_KEY.comment-properties")

/**
 * Get the list of image properties.
 */
val ApplicationConfig.imageProperties: List<Property>
    get() = getProperties("$ROOT_KEY.image-properties")

private fun ApplicationConfig.getProperties(key: String): List<Property> {
        val model = ModelFactory.createDefaultModel()
        val prefixes = usePrefixes.toMap()
        return property(key).getList().map { prop ->
            prefixes.entries.firstOrNull { entry -> prop.startsWith("${entry.key}:") }
                ?.let { model.createProperty(it.value, prop.substring(it.key.length + 1)) }
                ?: model.createProperty(prop)
        }
    }


// TODO test
val ApplicationConfig.softwareName: String
    get() = property("$ROOT_KEY.software-name").getString()




