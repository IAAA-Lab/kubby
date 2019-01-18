package es.iaaa.kubby.config

import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigurationException

const val DATASETS_KEY = "$ROOT_KEY.datasets"

/**
 * Known datasource types
 */

// TODO Move to the corresponding file
const val SPARQL_ENDPOINT = "SPARQL_ENDPOINT"

/**
 * Root of data source configurations.
 */
open class DataSourceConfiguration

/**
 * Get the list of defined datasets.
 * TODO Use a list of registered data source types
 */
val ApplicationConfig.datasets: List<DataSourceConfiguration>
    get() = configList(DATASETS_KEY).map {
        when (guessDataSourceType(it)) {
            SPARQL_ENDPOINT -> createSparqlEndpoint(it)
            else -> throw ApplicationConfigurationException("Unknown datasource type")
        }
    }

private fun guessDataSourceType(config: ApplicationConfig): String? {
    return if (config.propertyOrNull("sparql-endpoint") != null)
        SPARQL_ENDPOINT
    else
        null
}

/**
 * SPARQL endpoint configuration.
 * TODO Move to the corresponding file
 */
data class SPARQLEndpoint(
    val endpoint: String,
    val datasetBase: String,
    val defaultGraph: String?,
    val trustEndpoint: Boolean
) : DataSourceConfiguration()

private fun createSparqlEndpoint(config: ApplicationConfig) =
    with(config) {
        SPARQLEndpoint(
            endpoint = property("sparql-endpoint").getString(),
            datasetBase = property("dataset-base").getString(),
            trustEndpoint = propertyOrNull("trust-endpoint")?.getString()?.toBoolean() ?: false,
            defaultGraph = propertyOrNull("default-graph")?.getString()
        )
    }

