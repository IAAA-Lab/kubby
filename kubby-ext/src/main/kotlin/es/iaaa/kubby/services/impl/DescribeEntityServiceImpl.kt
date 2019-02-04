package es.iaaa.kubby.services.impl

import es.iaaa.kubby.rdf.mergePrefixes
import es.iaaa.kubby.rdf.prunePrefixes
import es.iaaa.kubby.repository.EntityId
import es.iaaa.kubby.repository.EntityRepository
import es.iaaa.kubby.services.DescribeEntityService

/**
 * Default implementation of [DescribeEntityService].
 */
class DescribeEntityServiceImpl(
    private val entityRepository: EntityRepository,
    private val prefixes: Map<String, String>
) : DescribeEntityService {

    override fun findOne(baseUri: String, localPart: String) =
        entityRepository.findOne(EntityId(baseUri, localPart)).apply {
            model.mergePrefixes(prefixes)
            model.prunePrefixes()
        }
}