package es.iaaa.kubby.services.impl

import es.iaaa.kubby.fixtures.Models.johnSmith
import es.iaaa.kubby.repository.EntityId
import es.iaaa.kubby.repository.EntityRepository
import io.mockk.every
import io.mockk.mockk
import kotlin.test.*

class DescribeEntityServiceImplTest {

    lateinit var repository: EntityRepository

    @BeforeTest
    fun before() {
        repository = mockk()
        every { repository.findOne(EntityId("http://source/", "JohnSmith")) } returns johnSmith()

    }

    @Test
    fun `find one resource`() {
        val service = DescribeEntityServiceImpl(repository, emptyMap())
        val resource = service.findOne("http://source/", "JohnSmith")
        assertTrue(johnSmith().model.isIsomorphicWith(resource.model))
    }

    @Test
    fun `merge prefixes`() {
        val service = DescribeEntityServiceImpl(repository, mapOf("src" to "http://source/"))
        val resource = service.findOne("http://source/", "JohnSmith")
        assertEquals("http://source/", resource.model.getNsPrefixURI("src"))
    }

    @Test
    fun `prune ns prefixes`() {
        val service = DescribeEntityServiceImpl(repository, mapOf("ns0" to "http://source/"))
        val resource = service.findOne("http://source/", "JohnSmith")
        assertNull(resource.model.getNsPrefixURI("ns0"))
    }
}