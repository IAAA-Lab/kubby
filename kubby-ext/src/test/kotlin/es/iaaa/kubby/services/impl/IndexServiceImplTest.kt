package es.iaaa.kubby.services.impl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IndexServiceImplTest {

    @Test
    fun `index when the index uri has value`() {
        val service = IndexServiceImpl("DBpedia")
        assertEquals("DBpedia", service.indexLocalPart())
    }

    @Test
    fun `no index when the index is null`() {
        val service = IndexServiceImpl(null)
        assertNull(service.indexLocalPart())
    }
}