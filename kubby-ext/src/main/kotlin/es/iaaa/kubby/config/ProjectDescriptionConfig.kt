package es.iaaa.kubby.config

import com.typesafe.config.Config
import es.iaaa.kubby.rest.api.Routes
import es.iaaa.kubby.text.replacePrefix
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigurationException
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property

/**
 * Description of the project.
 */
data class ProjectDescription(
    val projectName: String?,
    val projectHomepage: String?,
    val usePrefixes: Map<String, String>,
    val defaultLanguage: String,
    val labelProperties: List<Property>,
    val commentProperties: List<Property>,
    val imageProperties: List<Property>,
    val softwareName: String,
    val supportedLanguages: List<String>,
    val language: Map<String, Map<String, List<String>>>,
    val indexLocalPart: String?
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
        val usePrefixes = node.getConfig("usePrefixes")
            .entrySet().map { it.key to it.value.unwrapped().toString() }.toMap()
        val supportedLanguages = node.getStringList("supported-languages")
        val languageProperties = node.getStringList("language-data.properties")
        val language = supportedLanguages.associate { lang ->
            lang to languageProperties
                .associate { prop -> prop to node.getStringList("language-data.$lang.$prop") }
        }
        val indexLocalPart = runCatching { node.getString("indexResource") }.getOrNull()?.let  { uri ->
            runCatching { toPrefixes().find { uri.startsWith(it) } }.getOrNull()?.let { prefix ->
                uri.replacePrefix(prefix, "")
            }
        }
        node.run {
            return ProjectDescription(
                projectName = runCatching { getString("projectName") }.getOrNull(),
                projectHomepage = runCatching { getString("projectHomepage") }.getOrNull(),
                usePrefixes = usePrefixes,
                defaultLanguage = getString("defaultLanguage"),
                indexLocalPart = indexLocalPart,
                supportedLanguages = getStringList("supported-languages"),
                labelProperties = getStringList("labelProperty").toProperties(usePrefixes),
                commentProperties = getStringList("commentProperty").toProperties(usePrefixes),
                imageProperties = getStringList("imageProperty").toProperties(usePrefixes),
                softwareName = getString("software-name"),
                language = language
            )
        }
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