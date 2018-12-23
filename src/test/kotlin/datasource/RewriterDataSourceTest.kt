package es.iaaa.kubby.datasource

import io.mockk.mockk
import org.apache.jena.datatypes.TypeMapper
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.VCARD
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class RewriterDataSourceTest {

    val anyDs: DataSource = mockk(relaxed = true)

    @Test
    fun `Rewrite a URI that matches the target namespace`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val uri = rdw.rewrite("http://target/", "http://source/good")
        assertEquals("http://target/good", uri)
    }

    @Test
    fun `Don't Rewrite a URI that does not match the target namespace`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val uri = rdw.rewrite("http://target/", "http://other/good")
        assertEquals("http://other/good", uri)
    }

    @Test
    fun `Rewrite a subject that matches the target namespace`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val subject = model.createResource("http://source/good")
        val result = rdw.rewriteResource(subject, "http://target/", model).uri
        assertEquals("http://target/good", result)
    }

    @Test
    fun `Don't rewrite a subject that does not match the target namespace`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val subject = model.createResource("http://other/good")
        val result = rdw.rewriteResource(subject, "http://target/", model).uri
        assertEquals("http://other/good", result)
    }

    @Test
    fun `Keep bnodes as subject unaffected`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val subject = model.createResource()
        val result = rdw.rewriteResource(subject, "http://target/", model).uri
        assertEquals(null, result)
    }

    @Test
    fun `Rewrite a predicate that matches the target namespace`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val property = model.createProperty("http://source/good")
        val result = rdw.rewriteProperty(property, "http://target/", model).uri
        assertEquals("http://target/good", result)
    }

    @Test
    fun `Don't rewrite a predicate that does not match the target namespace`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val property = model.createProperty("http://other/good")
        val result = rdw.rewriteProperty(property, "http://target/", model).uri
        assertEquals("http://other/good", result)
    }

    @Test
    fun `Rewrite a object URI that matches the target namespace`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val obj = model.createResource("http://source/good")
        val result = rdw.rewriteRDFNode(obj, "http://target/", model)
        if (result is Resource) {
            assertEquals("http://target/good", result.uri)
        } else {
            fail("Not a resource")
        }
    }

    @Test
    fun `Don't rewrite a object URI that does not match the target namespace`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val obj = model.createResource("http://other/good")
        val result = rdw.rewriteRDFNode(obj, "http://target/", model)
        if (result is Resource) {
            assertEquals("http://other/good", result.uri)
        } else {
            fail("Not a resource")
        }
    }

    @Test
    fun `Plain literals are unaffected`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val obj = model.createTypedLiteral("http://source/good", null as String?)
        val result = rdw.rewriteRDFNode(obj, "http://target/", model)
        if (result is Literal) {
            assertEquals("http://source/good", result.lexicalForm)
            assertNull(result.datatype)
        } else {
            fail("Not a resource")
        }
    }

    @Test
    fun `Keep bnodes as  object unaffected`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val obj = model.createResource()
        val result = rdw.rewriteRDFNode(obj, "http://target/", model)
        assertTrue(result.isAnon)
    }

    @Test
    fun `Rewrite datatypes of typed literals that matches the target namespace`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val obj = model.createTypedLiteral("http://source/good", "http://source/good")
        val result = rdw.rewriteRDFNode(obj, "http://target/", model)
        if (result is Literal) {
            assertEquals("http://source/good", result.lexicalForm)
            assertEquals("http://target/good", result.datatype.uri)
        } else {
            fail("Not a resource")
        }
    }

    @Test
    fun `Don't rewrite datatypes of typed literals that does not match the target namespace`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val model = ModelFactory.createDefaultModel()
        val obj = model.createLiteral("http://source/good")
        val result = rdw.rewriteRDFNode(obj, "http://target/", model)
        if (result is Literal) {
            assertEquals("http://source/good", result.lexicalForm)
            assertEquals(TypeMapper.getInstance().getTypeByValue("http://source/good").uri, result.datatype.uri)
        } else {
            fail("Not a resource")
        }
    }

    @Test
    fun `Rewrite a simple model`() {
        val rdw = RewriterDataSource(anyDs, "http://source/")
        val result = rdw.rewrite("http://target/", aSimpleModel())
        assertTrue(result.contains(result.createResource("http://target/JohnSmith"), VCARD.FN, "John Smith"))
    }

    fun aSimpleModel(): Model {
        val model = ModelFactory.createDefaultModel()
        val johnSmith = model.createResource("http://source/JohnSmith")
        johnSmith.addProperty(VCARD.FN, "John Smith")
        return model
    }
}