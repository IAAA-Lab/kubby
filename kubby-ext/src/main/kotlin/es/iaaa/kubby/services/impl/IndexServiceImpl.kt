package es.iaaa.kubby.services.impl

import es.iaaa.kubby.repository.EntityRepository
import es.iaaa.kubby.services.IndexService

/**
 * Default implementation of [IndexService].
 */
class IndexServiceImpl(
    repository: EntityRepository,
    uri: String?
) : IndexService {
    private val _index = uri?.let {
        val index = repository.localId(it)
        if (index != it) index else null
    }

    override fun indexLocalPart() = _index
}
