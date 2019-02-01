package es.iaaa.kubby.repository

import es.iaaa.kubby.repository.source.*
import java.nio.file.Path

/**
 * Root of data source configurations.
 */
open class DataSourceConfiguration

/**
 * SPARQL service configuration.
 */

data class SparqlEndpoint(
    val service: String,
    val dataset: String?,
    val forceTrust: Boolean,
    val namespace: String,
    val addSameAs: Boolean
) : DataSourceConfiguration()

/**
 * TDB 2 configuration.
 */
data class Tdb2Location(
    val path: Path,
    val mode: DatasourceMode = DatasourceMode.CONNECT,
    val data: Path,
    val namespace: String,
    val addSameAs: Boolean
) : DataSourceConfiguration()

/**
 * The interface of a factory that creates an [EntityRepository] from a list of [DataSourceConfiguration].
 */
interface EntityRepositoryFactory {
    fun createFrom(list: List<DataSourceConfiguration>): EntityRepository
}

/**
 * Basic factory.
 */
object BasicEntityRepositoryFactory : EntityRepositoryFactory {
    override fun createFrom(list: List<DataSourceConfiguration>) = if (list.isNotEmpty())
        MergeEntityRepository(list.map { retrieveEntityRepository(it) })
    else
        throw EntityRepositoryException("Requires at least one datasource")

    private fun retrieveEntityRepository(config: DataSourceConfiguration) = when (config) {
        is SparqlEndpoint -> RewrittenEntityRepository(
            repository = SparqlEntityRepository(
                service = config.service,
                dataset = config.dataset,
                forceTrust = config.forceTrust
            ),
            namespace = config.namespace,
            addSameAs = config.addSameAs
        )
        is Tdb2Location -> RewrittenEntityRepository(
            repository = Tdb2EntityRepository(
                path = config.path,
                mode = config.mode,
                data = config.data
            ),
            namespace = config.namespace,
            addSameAs = config.addSameAs
        )
        else -> throw EntityRepositoryException("Unknown configuration")
    }
}

/**
 * Exception thrown during the creation of an [EntityRepository].
 */
class EntityRepositoryException(msg: String) : Exception(msg)


