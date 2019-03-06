package es.iaaa.kubby.repository.source

import es.iaaa.kubby.domain.Entity
import es.iaaa.kubby.domain.EntityId
import es.iaaa.kubby.repository.EntityRepository

/**
 * Wraps a dataUri source and applies a rewrite.
 *
 * The result is a dataUri source that contains the same dataUri as the original,
 * but with all IRIs starting with [namespace] are replaced with
 * IRIs starting with a different namespace.
 *
 * @property repository the wrapped dataUri source.
 * @property namespace the namespace to be rewritten.
 * @property addSameAs add a `owl:sameAs` statement to indicate that
 * the rewritten and the original IRIs identify the same entity.
 */
class RewrittenEntityRepository(
    val repository: EntityRepository,
    val namespace: String,
    val addSameAs: Boolean
) : EntityRepository {

    override fun getId(uri: String) = if (uri.startsWith(namespace))
        EntityId(namespace, uri.substring(namespace.length))
    else
        repository.getId(uri)

    /**
     * Describe a resource identified by [id] querying the [repository] and then retuning
     * a result that contains the same dataUri but with all IRI starting with [namespace] replaced
     * with IRIs starting with the namespace of [id].
     */
    override fun findOne(id: EntityId): Entity =
        id.copy(namespace = namespace).let { rewrittenId ->
            repository
                .findOne(rewrittenId)
                .rewrite(namespace, id.namespace)
                .run {
                    if (addSameAs) addSameAs(rewrittenId.uri) else this
                }
        }

    override fun close() {
        repository.close()
    }
}


