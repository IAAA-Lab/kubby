package es.iaaa.kubby.repository.source

import es.iaaa.kubby.rdf.merge
import es.iaaa.kubby.repository.EntityId
import es.iaaa.kubby.repository.EntityRepository
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource

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
        .map { it.getId(uri) }
        .find { it.namespace.isNotEmpty() }
        ?: EntityId(localPart = uri)

    /**
     * Map and merge the responses from the [repositories].
     */
    override fun findOne(id: EntityId): Resource = repositories
        .map { it.findOne(id).model ?: ModelFactory.createDefaultModel() }
        .reduce { r, m -> r.merge(m) }
        .getResource(id.uri)

    /**
     * Closes underlying [repositories].
     */
    override fun close() {
        repositories.forEach { it.close() }
    }
}
