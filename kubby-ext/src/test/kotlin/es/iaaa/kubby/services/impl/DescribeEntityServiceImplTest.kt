package es.iaaa.kubby.services.impl

import es.iaaa.kubby.domain.EntityId
import es.iaaa.kubby.fixtures.Models.johnSmith
import es.iaaa.kubby.repository.EntityRepository
import io.mockk.every
import io.mockk.mockk
import org.apache.jena.rdf.model.Model
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DescribeEntityServiceImplTest {

    private lateinit var repository: EntityRepository

    @BeforeTest
    fun before() {
        repository = mockk()
        every { repository.findOne(EntityId("http://source/", "JohnSmith")) } returns johnSmith()

    }

    @Test
    fun `find one resource`() {
        val service = DescribeEntityServiceImpl(repository, emptyMap())
        val entity = service.findOne("http://source/", "JohnSmith")
        assertTrue(johnSmith().model.isIsomorphicWith(entity.toGraphModel() as Model))
    }

    @Test
    fun `merge prefixes`() {
        val service = DescribeEntityServiceImpl(repository, mapOf("src" to "http://source/"))
        val entity = service.findOne("http://source/", "JohnSmith")
        assertEquals("http://source/", (entity.toGraphModel() as Model).getNsPrefixURI("src"))
    }
}