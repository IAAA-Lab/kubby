package es.iaaa.kubby.repository.source

import es.iaaa.kubby.repository.Entity
import es.iaaa.kubby.repository.EntityId
import es.iaaa.kubby.repository.EntityRepository

/**
 * [EntityRepository] backed by other [repositories] that map and reduces operations.
 * It requires a non empty list of [repositories].
 */
class MergeEntityRepository(private val repositories: List<EntityRepository>) :
    EntityRepository {

    /**
     * Returns an [EntityId] with a non empty namespace if possible.
     */
    override fun getId(uri: String) = repositories
        .asSequence()
        .map { it.getId(uri) }
        .find { it.namespace.isNotEmpty() }
        ?: EntityId(localPart = uri)

    /**
     * Map and merge the responses from the [repositories].
     */
    override fun findOne(id: EntityId): Entity = repositories
            .map { it.findOne(id) }
            .filter { !it.isEmpty }
            .fold(id.toEntity()) { acc, m -> acc.merge(m) }

    /**
     * Closes underlying [repositories].
     */
    override fun close() {
        repositories.forEach { it.close() }
    }
}

