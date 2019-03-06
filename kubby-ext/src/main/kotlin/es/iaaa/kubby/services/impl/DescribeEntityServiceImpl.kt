package es.iaaa.kubby.services.impl

import es.iaaa.kubby.repository.EntityRepository
import es.iaaa.kubby.services.DescribeEntityService

/**
 * Default implementation of [DescribeEntityService].
 */
class DescribeEntityServiceImpl(
    private val entityRepository: EntityRepository,
    private val prefixes: Map<String, String>
) : DescribeEntityService {

    override fun findOne(baseUri: String, localPart: String) = entityRepository
        .findOne(baseUri, localPart)
        .merge(prefixes)
}