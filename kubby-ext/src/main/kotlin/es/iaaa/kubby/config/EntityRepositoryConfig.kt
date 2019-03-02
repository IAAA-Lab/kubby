package es.iaaa.kubby.config

import com.typesafe.config.Config
import es.iaaa.kubby.config.SourceType.*
import es.iaaa.kubby.repository.EntityRepository
import es.iaaa.kubby.repository.source.*
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigurationException
import java.nio.file.Paths

/**
 * Supported sources.
 */

enum class SourceType {
    SPARQL, TDB2, UNKNOWN
}

/**
 * Create an [EntityRepository] from the [ApplicationConfig].
 */
fun Config.toEntityRepository(): EntityRepository =
    getConfigList("kubby.dataset").let {
        if (it.isNotEmpty())
            MergeEntityRepository(it.toEntityRepositories())
        else
            throw EntityRepositoryException("Requires at least one datasource")
    }


/**
 * Exception thrown during the creation of an [EntityRepository].
 */
class EntityRepositoryException(msg: String) : Exception(msg)


/**
 * Maps the [List] of configuration nodes to a list of [EntityRepository].
 */
internal fun List<Config>.toEntityRepositories(): List<EntityRepository> =
    map {
        RewrittenEntityRepository(
            namespace = it.getString("datasetBase"),
            addSameAs = runCatching { it.getBoolean("addSameAsStatements") }.getOrDefault(false),
            repository = when (heuristic(it)) {
                SPARQL -> it.toSparqlEntityRepository()
                TDB2 -> it.toTDB2EntityRepository()
                UNKNOWN -> throw ApplicationConfigurationException("Unknown datasource type")
            }
        )
    }

private fun heuristic(config: Config) =
    if (runCatching { config.getString("sparqlEndpoint") }.isSuccess) {
        SPARQL
    } else if (runCatching { config.getString("type") == "tdb2" }.isSuccess) {
        TDB2
    } else {
        UNKNOWN
    }


/**
 * Create a SPARQL endpoint from the node.
 */
internal fun Config.toSparqlEntityRepository() =
    SparqlEntityRepository(
        service = getString("sparqlEndpoint"),
        dataset = runCatching { getString("sparqlDefaultGraph") }.getOrNull(),
        forceTrust = runCatching { getBoolean("trustEndpoint") }.getOrDefault(false)
    )

/**
 * Create a TDB2 store from the node.
 */
internal fun Config.toTDB2EntityRepository() =
    Tdb2EntityRepository(
        path = toPath("path"),
        mode = toMode("mode"),
        data = toPath("data", "data.ttl")
    )

/**
 * Helper extension that converts any string in [key] to a valid or safe [RepositoryMode].
 */
private fun Config.toMode(key: String) = RepositoryMode.valueOf(
    runCatching { getString(key).toUpperCase() }
        .getOrDefault("CONNECT")
)

/**
 * Helper extension that converts any string in [key] to a [Path].
 * The [key] is required unless a [devault] value is set.
 */
private fun Config.toPath(key: String, default: String? = null) = Paths.get(
    if (default == null) {
        runCatching { getString(key) }.getOrDefault(default)
    } else {
        getString(key)
    }
)

