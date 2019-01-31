package es.iaaa.kubby.services.impl

import es.iaaa.kubby.repository.EntityRepository
import es.iaaa.kubby.services.IndexService

/**
 * Default implementation of [IndexService].
 */
class DefaultIndexServiceImpl(
    repository: EntityRepository,
    uri: String?
) : IndexService {
    private val _index = uri?.let {
        with(repository.getId(it)) {
            if (qualified) localPart else null
        }
    }

    override fun indexLocalPart() = _index
}
