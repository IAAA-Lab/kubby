package es.iaaa.kubby.repository

import es.iaaa.kubby.domain.Entity
import org.apache.jena.rdf.model.Model
import java.io.Closeable


/**
 * A facade interface for the repositories.
 *
 * This allows to provide several implementations.
 */
interface EntityRepository : Closeable {

    /**
     * Extracts the local identifier from the [uri] in this repository.
     */
    fun localId(uri: String): String

    /**
     * Gets the [Model] describing one resource based on the [namespace] and the [localId].
     */
    fun findOne(namespace: String, localId: String): Entity
}

