package es.iaaa.kubby.repository.source

import es.iaaa.kubby.rdf.addSameAs
import es.iaaa.kubby.rdf.rewrite
import es.iaaa.kubby.repository.EntityId
import es.iaaa.kubby.repository.EntityRepository

/**
 * Wraps a data source and applies a rewrite.
 *
 * The result is a data source that contains the same data as the original,
 * but with all IRIs starting with [namespace] are replaced with
 * IRIs starting with a different namespace.
 *
 * @property repository the wrapped data source.
 * @property namespace the namespace to be rewritten.
 * @property addSameAs add a `owl:sameAs` statement to indicate that
 * the rewritten and the original IRIs identify the same entity.
 */
class RewrittenEntityRepository(
    private val repository: EntityRepository,
    private val namespace: String,
    private val addSameAs: Boolean
) : EntityRepository {

    override fun getId(uri: String) = if (uri.startsWith(namespace)) {
        EntityId(namespace, uri.substring(namespace.length))
    } else {
        repository.getId(uri)
    }

    /**
     * Describe a resource identified by [id] querying the [repository] and then retuning
     * a result that contains the same data but with all IRI starting with [namespace] replaced
     * with IRIs starting with the namespace of [id].
     */
    override fun findOne(id: EntityId) =
        id.copy(namespace = namespace).let { rewrittenId ->
            repository
                .findOne(rewrittenId)
                .model
                .rewrite(namespace, id.namespace)
                .getResource(id.uri)
                .apply {
                    if (addSameAs && namespace != id.namespace && !model.isEmpty) {
                        addSameAs(rewrittenId.uri)
                    }
                }

        }

    override fun close() {
        repository.close()
    }
}


