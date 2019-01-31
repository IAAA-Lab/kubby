package es.iaaa.kubby.repository

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import java.io.Closeable

/**
 * A facade interface for the repositories.
 *
 * This allows to provide several implementations.
 */
interface EntityRepository : Closeable {

    /**
     * Generate the [EntityId] representation for a [uri] in this repository.
     */
    fun getId(uri: String): EntityId


    /**
     * Get the [Model] describing one resource based on the [id].
     */
    fun findOne(id: EntityId): Resource
}

/**
 * Qualified identifier composed of a [namespace] and a [localPart].
 */
data class EntityId(
    val namespace: String = "",
    val localPart: String
) {
    val qualified = namespace != ""
    val uri = "$namespace$localPart"
    override fun toString() = "{$namespace}$localPart"
}
