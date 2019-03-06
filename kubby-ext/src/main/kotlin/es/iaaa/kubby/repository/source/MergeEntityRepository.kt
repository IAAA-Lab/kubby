package es.iaaa.kubby.repository.source

import es.iaaa.kubby.domain.Entity
import es.iaaa.kubby.domain.NullEntity
import es.iaaa.kubby.repository.EntityRepository

/**
 * [EntityRepository] backed by other [repositories] that map and reduces operations.
 * It requires a non empty list of [repositories].
 */
class MergeEntityRepository(private val repositories: List<EntityRepository>) :
    EntityRepository {

    /**
     * Finds first candidate to be the local identifier.
     */
    override fun localId(uri: String) : String = repositories
        .asSequence()
        .map { it.localId(uri) }
        .find { it != uri }
        ?: uri

    /**
     * Map and merge the responses from the [repositories].
     */
    override fun findOne(namespace: String, localId: String): Entity = repositories
        .map { it.findOne(namespace, localId) }
        .ifEmpty { listOf(NullEntity("$namespace$localId")) }
        .reduce { acc, m -> acc.merge(m) }

    /**
     * Closes underlying [repositories].
     */
    override fun close() {
        repositories.forEach { it.close() }
    }
}

