package es.iaaa.kubby.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property


data class Route(
    val resource: String,
    val data : String,
    val page : String
)

data class LocaleData(
    val uncapitalizedWords: Set<String>
)

object Configuration {
    val config : Config = ConfigFactory.load()
    val route = config.extract<Route>("kubby.route")
    val locales = config.extract<Map<String, LocaleData>>("kubby.locale-data")
    val defaultLocale = config.extract<String>("kubby.locale-default")
    val prefixes = config.extract<Map<String,String>>("kubby.prefix-declarations")
    val labelProperties: List<Property>
    init {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefixes(prefixes)
        labelProperties = config.extract<List<String>>("kubby.label-properties").map {
            val match = prefixes.entries.firstOrNull { entry -> it.startsWith("${entry.key}:") }
            if (match  != null) {
                model.createProperty(match.value, it.substring(match.key.length + 1))
            } else  {
                model.createProperty(it)
            }
        }
    }
}

