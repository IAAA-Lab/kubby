package es.iaaa.kubby.config

import io.ktor.config.ApplicationConfig
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property


abstract class Source
data class SparqlEndpoint(
    val endpoint: String,
    val datasetBase: String,
    val defaultGraph: String? = null,
    val trustEndpoint: Boolean = false
) : Source()


/**
 * Get the project name.
 */
val ApplicationConfig.projectName: String
    get() = config("kubby").property("project-name").getString()

/**
 * Get the project homepage.
 */
val ApplicationConfig.projectHomepage: String
    get() = config("kubby").property("project-homepage").getString()

/**
 * Get the project used prefixes.
 *
 * TODO Ensure that these prefixes are added to the model after retrieval
 *
 */
val ApplicationConfig.usePrefixes: Map<String, String>
    get() = configList("kubby.use-prefixes").map {
        val prefix = it.property("prefix").getString()
        val uri = it.property("uri").getString()
        prefix to uri
    }.toMap()

/**
 * Get the default language of the project.
 */
val ApplicationConfig.defaultLanguage: String
    get() = config("kubby").property("default-language").getString()

/**
 * Get the index resource of the project.
 */
val ApplicationConfig.indexResource: String?
    get() = config("kubby").propertyOrNull("index-resource")?.getString()

/**
 * Get the list of defined datasets.
 */
val ApplicationConfig.datasets: List<Source>
    get() = configList("kubby.datasets").map {
        SparqlEndpoint(
            endpoint = it.property("sparql-endpoint").getString(),
            datasetBase = it.property("dataset-base").getString(),
            trustEndpoint = it.propertyOrNull("trust-endpoint")?.getString()?.toBoolean() ?: false,
            defaultGraph = it.propertyOrNull("default-graph")?.getString()
        )
    }
/**
 * Get the list of label properties.
 */
val ApplicationConfig.labelProperties: List<Property>
    get() {
        val model = ModelFactory.createDefaultModel()
        val prefixes = usePrefixes.toMap()
        return property("kubby.label-properties").getList().map { prop ->
            prefixes.entries.firstOrNull { entry -> prop.startsWith("${entry.key}:") }
                ?.let { model.createProperty(it.value, prop.substring(it.key.length + 1)) }
                ?: model.createProperty(prop)
        }
    }

// TODO test
fun ApplicationConfig.text(key: String, lang: String) =
    property("kubby.language-data.$lang.$key").getString()

// TODO test
fun ApplicationConfig.list(key: String, lang: String) =
    propertyOrNull("kubby.language-data.$lang.$key")?.getList() ?: emptyList()

// TODO test
val ApplicationConfig.softwareName: String
    get() = property("kubby.name").getString()

// TODO test
val ApplicationConfig.dataPath: String
    get() = property("kubby.route.data").getString()

// TODO test
val ApplicationConfig.resourcePath: String
    get() = property("kubby.route.resource").getString()

// TODO test
val ApplicationConfig.pagePath: String
    get() = property("kubby.route.page").getString()

// TODO test
val ApplicationConfig.aboutPath: String
    get() = property("kubby.route.about").getString()

