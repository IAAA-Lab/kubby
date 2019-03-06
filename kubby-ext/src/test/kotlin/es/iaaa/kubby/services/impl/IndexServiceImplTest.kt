package es.iaaa.kubby.services.impl

import es.iaaa.kubby.repository.EntityRepository
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IndexServiceImplTest {

    lateinit var repository: EntityRepository

    @BeforeTest
    fun before() {
        repository = mockk()
    }

    @Test
    fun `index when the index uri has local part`() {
        every { repository.localId("http://localhost/dbpedia/DBpedia") } returns "DBpedia"
        val service = IndexServiceImpl(repository, "http://localhost/dbpedia/DBpedia")
        assertEquals("DBpedia", service.indexLocalPart())
    }

    @Test
    fun `no index when the index uri is not qualified`() {
        every { repository.localId("http://localhost/dbpedia/DBpedia") } returns "http://localhost/dbpedia/DBpedia"
        val service = IndexServiceImpl(repository, "http://localhost/dbpedia/DBpedia")
        assertNull(service.indexLocalPart())
    }

    @Test
    fun `no index when the index is null`() {
        val service = IndexServiceImpl(repository, null)
        assertNull(service.indexLocalPart())
    }
}