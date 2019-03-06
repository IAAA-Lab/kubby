package es.iaaa.kubby.services

import es.iaaa.kubby.domain.Entity

/**
 * Describes an entity.
 */
interface DescribeEntityService {
    /**
     * Returns an entity whose uri starts with [baseUri] and ends with [localPart].
     */
    fun findOne(baseUri: String, localPart: String): Entity
}