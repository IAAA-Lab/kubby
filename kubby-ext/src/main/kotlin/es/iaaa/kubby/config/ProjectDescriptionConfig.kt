package es.iaaa.kubby.config

import com.typesafe.config.Config
import es.iaaa.kubby.rest.api.Routes
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigurationException
import io.ktor.config.tryGetString
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property

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
    val indexResource: String?
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
fun Config.toProjectDescription(): ProjectDescription {
    getConfig("kubby").let { node ->
        val usePrefixes = node.getConfigList("use-prefixes").map {
            val prefix = it.getString("prefix")
            val uri = it.getString("uri")
            prefix to uri
        }.toMap()
        val supportedLanguages = node.getStringList("supported-languages")
        val languageProperties = node.getStringList("language-data.properties")
        val language = supportedLanguages.associate { lang ->
            lang to languageProperties
                .associate { prop -> prop to node.getStringList("language-data.$lang.$prop") }
        }
        return ProjectDescription(
            projectName = node.tryGetString("project-name") ?: "",
            projectHomepage = node.tryGetString("project-homepage") ?: "",
            usePrefixes = usePrefixes,
            defaultLanguage = node.getString("default-language"),
            supportedLanguages = node.getStringList("supported-languages"),
            labelProperties = node.getStringList("label-properties").toProperties(usePrefixes),
            commentProperties = node.getStringList("comment-properties").toProperties(usePrefixes),
            imageProperties = node.getStringList("image-properties").toProperties(usePrefixes),
            softwareName = node.getString("software-name"),
            language = language,
            indexResource = node.tryGetString("index-resource")
        )
    }
}

/**
 * Maps the [ApplicationConfig] into a [Routes] object.
 */
fun Config.toRoutes() =
    getConfig("kubby.route").let {
        Routes(
            pagePath = it.getString("page"),
            dataPath = it.getString("data"),
            resourcePath = it.getString("resource")
        )
    }


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