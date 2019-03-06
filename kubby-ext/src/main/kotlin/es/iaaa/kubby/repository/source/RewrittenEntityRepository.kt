package es.iaaa.kubby.repository.source

import es.iaaa.kubby.domain.Entity
import es.iaaa.kubby.domain.EntityId
import es.iaaa.kubby.repository.EntityRepository

/**
 * Wraps a dataUri source and applies a rewrite.
 *
 * The result is a dataUri source that contains the same dataUri as the original,
 * but with all IRIs starting with [prefix] are replaced with
 * IRIs starting with a different prefix.
 *
 * @property repository the wrapped dataUri source.
 * @property prefix the prefix to be rewritten.
 * @property addSameAs add a `owl:sameAs` statement to indicate that
 * the rewritten and the original IRIs identify the same entity.
 */
class RewrittenEntityRepository(
    val repository: EntityRepository,
    val prefix: String,
    val addSameAs: Boolean
) : EntityRepository {

    override fun getId(uri: String) = if (uri.startsWith(prefix))
        EntityId(prefix, uri.substring(prefix.length))
    else
        repository.getId(uri)

    /**
     * Describe a resource identified by [prefix] and [localId] by querying the [repository] and then retuning
     * a result that contains the same dataUri but with all IRI starting with [prefix] replaced
     * with IRIs starting with [namespace].
     */
    override fun findOne(namespace: String, localId: String): Entity =
            repository
                .findOne(prefix, localId)
                .rewrite(prefix, namespace)
                .run {
                    if (addSameAs) addSameAs("$prefix$localId") else this
                }

    override fun close() {
        repository.close()
    }
}


