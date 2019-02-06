package es.iaaa.kubby.config

import es.iaaa.kubby.repository.EntityRepository
import es.iaaa.kubby.repository.source.*
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigurationException
import java.nio.file.Paths

/**
 * Create an [EntityRepository] from the [ApplicationConfig].
 */
fun ApplicationConfig.toEntityRepository(): EntityRepository =
    configList("kubby.datasets").let {
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
internal fun List<ApplicationConfig>.toEntityRepositories(): List<EntityRepository> =
    map {
        RewrittenEntityRepository(
            namespace = it.property("dataset-base").getString(),
            addSameAs = it.propertyOrNull("add-same-as")?.getString()?.toBoolean() ?: false,
            repository = when (it.property("type").getString()) {
                "sparql" -> it.toSparqlEntityRepository()
                "tdb2" -> it.toTDB2EntityRepository()
                else -> throw ApplicationConfigurationException("Unknown datasource type")
            }
        )
    }

/**
 * Create a SPARQL endpoint from the node.
 */
internal fun ApplicationConfig.toSparqlEntityRepository() =
    SparqlEntityRepository(
        service = property("endpoint").getString(),
        dataset = propertyOrNull("default-graph")?.getString(),
        forceTrust = propertyOrNull("trust-endpoint")?.getString()?.toBoolean() ?: false
    )

/**
 * Create a TDB2 store from the node.
 */
internal fun ApplicationConfig.toTDB2EntityRepository() =
    Tdb2EntityRepository(
        path = Paths.get(property("path").getString()),
        mode = propertyOrNull("mode")?.getString().toMode(),
        data = propertyOrNull("dataUri")?.getString()?.let { Paths.get(it) } ?: Paths.get("dataUri.ttl")
    )

/**
 * Helper extension that converts any string to a valid or safe [RepositoryMode].
 */
private fun String?.toMode(): RepositoryMode =
    if (this == null)
        RepositoryMode.CONNECT
    else runCatching { RepositoryMode.valueOf(toUpperCase()) }
        .getOrDefault(RepositoryMode.CONNECT)
