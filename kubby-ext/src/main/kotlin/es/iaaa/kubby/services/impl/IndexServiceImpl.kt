package es.iaaa.kubby.services.impl

import es.iaaa.kubby.services.IndexService

/**
 * Default implementation of [IndexService].
 */
class IndexServiceImpl(
    private val indexLocalPart: String?
) : IndexService {
    override fun indexLocalPart() = indexLocalPart
}
