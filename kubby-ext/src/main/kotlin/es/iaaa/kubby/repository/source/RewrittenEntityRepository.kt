package es.iaaa.kubby.repository.source

import es.iaaa.kubby.rdf.addSameAs
import es.iaaa.kubby.rdf.rewrite
import es.iaaa.kubby.repository.Entity
import es.iaaa.kubby.repository.EntityId
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
                .apply {
                    if (addSameAs && namespace != id.namespace && !isEmpty) addSameAs(rewrittenId)
                }
        }

    override fun close() {
        repository.close()
    }
}

/**
 * Rewrites an Entity from a [old] namespace to a [new] namespace.
 */
private fun Entity.rewrite(old: String, new: String) = resource.run {
        val newUri = rewrite(old, new).uri
        Entity(model.rewrite(old, new).getResource(newUri), attribution)
    }

/**
 * Adds owl:sameAs statement between this [Entity] and the [Entity] identified by [id].
 */
private fun Entity.addSameAs(id: EntityId) = resource.run { addSameAs(id.uri) }
