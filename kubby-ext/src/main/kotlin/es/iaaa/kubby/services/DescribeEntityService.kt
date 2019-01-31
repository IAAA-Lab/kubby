package es.iaaa.kubby.services

import org.apache.jena.rdf.model.Resource

/**
 * Describes an entity.
 */
interface DescribeEntityService {
    /**
     * Returns an entity whose uri starts with [baseUri] and ends with [localPart].
     */
    fun findOne(baseUri: String, localPart: String): Resource
}