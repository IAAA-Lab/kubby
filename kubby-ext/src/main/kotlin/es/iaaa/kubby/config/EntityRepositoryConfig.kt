package es.iaaa.kubby.config

import com.typesafe.config.Config
import es.iaaa.kubby.repository.EntityRepository
import es.iaaa.kubby.repository.source.*
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigurationException
import io.ktor.config.tryGetString
import java.nio.file.Paths

/**
 * Create an [EntityRepository] from the [ApplicationConfig].
 */
fun Config.toEntityRepository(): EntityRepository =
    getConfigList("kubby.datasets").let {
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
            namespace = it.getString("dataset-base"),
            addSameAs = it.tryGetString("add-same-as")?.toBoolean() ?: false,
            repository = when (it.getString("type")) {
                "sparql" -> it.toSparqlEntityRepository()
                "tdb2" -> it.toTDB2EntityRepository()
                else -> throw ApplicationConfigurationException("Unknown datasource type")
            }
        )
    }

/**
 * Create a SPARQL endpoint from the node.
 */
internal fun Config.toSparqlEntityRepository() =
    SparqlEntityRepository(
        service = getString("endpoint"),
        dataset = tryGetString("default-graph"),
        forceTrust = tryGetString("trust-endpoint")?.toBoolean() ?: false
    )

/**
 * Create a TDB2 store from the node.
 */
internal fun Config.toTDB2EntityRepository() =
    Tdb2EntityRepository(
        path = Paths.get(getString("path")),
        mode = tryGetString("mode").toMode(),
        data = tryGetString("dataUri")?.let { Paths.get(it) } ?: Paths.get("dataUri.ttl")
    )

/**
 * Helper extension that converts any string to a valid or safe [RepositoryMode].
 */
private fun String?.toMode(): RepositoryMode =
    if (this == null)
        RepositoryMode.CONNECT
    else runCatching { RepositoryMode.valueOf(toUpperCase()) }
        .getOrDefault(RepositoryMode.CONNECT)
