package es.iaaa.kubby.config

import es.iaaa.kubby.repository.DataSourceConfiguration
import es.iaaa.kubby.repository.SparqlEndpoint
import es.iaaa.kubby.repository.Tdb2Location
import es.iaaa.kubby.repository.source.DatasourceMode
import es.iaaa.kubby.rest.api.Routes
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigurationException
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import java.nio.file.Paths

/**
 * Description of the project.
 */
data class ProjectDescription(
    val projectName: String,
    val projectHomepage: String,
    val usePrefixes: Map<String, String>,
    val defaultLanguage: String,
    val labelProperties: List<Property>,
    val commentProperties: List<Property>,
    val imageProperties: List<Property>,
    val softwareName: String,
    val supportedLanguages: List<String>,
    val language: Map<String, Map<String, List<String>>>,
    val indexResource: String?,
    val datasets: List<DataSourceConfiguration>
) {
    /**
     * Get a i18n [List] identified by a [key] in the specified [language].
     */
    fun getLanguageList(key: String, language: String = defaultLanguage) =
        this.language[language]?.get(key) ?: throw ApplicationConfigurationException("Expected list at $language.$key")

    /**
     * Get a i18n [String] identified by a [key] in the specified [language].
     */
    fun getLanguageValue(key: String, language: String = defaultLanguage) =
        this.language[language]?.get(key)?.first()
            ?: throw ApplicationConfigurationException("Expected string at $language.$key")
}

/**
 * Maps the [ApplicationConfig] into a [ProjectDescription] object.
 */
fun ApplicationConfig.toProjectDescription(): ProjectDescription {
    config("kubby").let { node ->
        val usePrefixes = node.configList("use-prefixes").map {
            val prefix = it.property("prefix").getString()
            val uri = it.property("uri").getString()
            prefix to uri
        }.toMap()
        val supportedLanguages = node.property("supported-languages").getList()
        val languageProperties = node.property("language-data.properties").getList()
        val language = supportedLanguages.associate { lang ->
            lang to languageProperties
                .associate { prop -> prop to node.property("language-data.$lang.$prop").getList() }
        }
        return ProjectDescription(
            projectName = node.propertyOrNull("project-name")?.getString() ?: "",
            projectHomepage = node.propertyOrNull("project-homepage")?.getString() ?: "",
            usePrefixes = usePrefixes,
            defaultLanguage = node.property("default-language").getString(),
            supportedLanguages = node.property("supported-languages").getList(),
            labelProperties = node.property("label-properties").getList().toProperties(usePrefixes),
            commentProperties = node.property("comment-properties").getList().toProperties(usePrefixes),
            imageProperties = node.property("image-properties").getList().toProperties(usePrefixes),
            softwareName = node.property("software-name").getString(),
            language = language,
            indexResource = node.propertyOrNull("index-resource")?.getString(),
            datasets = node.configList("datasets").toMapToDataSources()
        )
    }
}

/**
 * Maps the [ApplicationConfig] into a [Routes] object.
 */
fun ApplicationConfig.toRoutes() =
    config("kubby.route").let {
        Routes(
            pagePath = it.property("page").getString(),
            dataPath = it.property("data").getString(),
            resourcePath = it.property("resource").getString()
        )
    }

/**
 * Maps the [List] of configuration nodes to a list of [DataSourceConfiguration]s.
 */
private fun List<ApplicationConfig>.toMapToDataSources(): List<DataSourceConfiguration> =
    map {
        when (it.property("type").getString()) {
            "sparql" -> it.createSparqlEndpoint()
            "tdb2" -> it.createTDB2Store()
            else -> throw ApplicationConfigurationException("Unknown datasource type")
        }
    }

/**
 * Create a SPARQL endpoint from the node.
 */
private fun ApplicationConfig.createSparqlEndpoint() =
    SparqlEndpoint(
        service = property("endpoint").getString(),
        dataset = propertyOrNull("default-graph")?.getString(),
        forceTrust = propertyOrNull("trust-endpoint")?.getString()?.toBoolean() ?: false,
        namespace = property("dataset-base").getString(),
        addSameAs = propertyOrNull("add-same-as")?.getString()?.toBoolean() ?: false
    )

/**
 * Create a TDB2 store from the node.
 */
private fun ApplicationConfig.createTDB2Store() =
    Tdb2Location(
        path = Paths.get(property("path").getString()),
        mode = propertyOrNull("mode")?.getString()?.let { DatasourceMode.valueOf(it) } ?: DatasourceMode.CONNECT,
        data = propertyOrNull("data")?.getString()?.let { Paths.get(it) } ?: Paths.get("data.ttl"),
        namespace = property("dataset-base").getString(),
        addSameAs = propertyOrNull("add-same-as")?.getString()?.toBoolean() ?: false
    )


/**
 * Transform a [List] of URIs possibly in CURIE form into a list of [Property] with the help of set of [prefixes].
 */
private fun List<String>.toProperties(prefixes: Map<String, String>): List<Property> {
    val model = ModelFactory.createDefaultModel()
    return map { prop ->
        prefixes.entries.firstOrNull { entry -> prop.startsWith("${entry.key}:") }
            ?.let { model.createProperty(it.value, prop.substring(it.key.length + 1)) }
            ?: model.createProperty(prop)
    }
}