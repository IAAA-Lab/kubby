package es.iaaa.kubby.repository

import es.iaaa.kubby.repository.source.*
import java.nio.file.Path

/**
 * Data source configurations.
 */
class DataSourceConfiguration(
    val namespace: String,
    val addSameAs: Boolean,
    val source: DataSource
)

/**
 * Data source root
 */

sealed class DataSource

/**
 * SPARQL service configuration.
 */

data class SparqlEndpoint(
    val service: String,
    val dataset: String?,
    val forceTrust: Boolean
) : DataSource()

/**
 * TDB 2 configuration.
 */
class Tdb2Location(
    val path: Path,
    val mode: DatasourceMode = DatasourceMode.CONNECT,
    val data: Path
) : DataSource()

/**
 * Factory as extension of a list of [DataSourceConfiguration].
 */
fun List<DataSourceConfiguration>.toEntityRepository() = if (isNotEmpty())
    MergeEntityRepository( map { retrieveEntityRepository(it) })
else
    throw EntityRepositoryException("Requires at least one datasource")

private fun retrieveEntityRepository(config: DataSourceConfiguration) = RewrittenEntityRepository(
    repository = buildRepository(config.source),
    namespace = config.namespace,
    addSameAs = config.addSameAs)

private fun buildRepository(config: DataSource) =
    when (config) {
        is SparqlEndpoint ->  SparqlEntityRepository(
            service = config.service,
            dataset = config.dataset,
            forceTrust = config.forceTrust
        )
        is Tdb2Location ->  Tdb2EntityRepository(
            path = config.path,
            mode = config.mode,
            data = config.data
        )
    }

/**
 * Exception thrown during the creation of an [EntityRepository].
 */
class EntityRepositoryException(msg: String) : Exception(msg)


