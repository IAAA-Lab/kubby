package es.iaaa.kubby.rdf

import es.iaaa.kubby.fixtures.Models.aSimpleModel
import org.apache.jena.rdf.model.ModelFactory.createDefaultModel
import org.apache.jena.vocabulary.VCARD
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelTest {

    @Test
    fun `rewrite a resource that matches the target namespace`() {
        val old = createDefaultModel().createResource("http://source/good")
        val new = old.rewrite("http://source/", "http://target/")
        assertEquals("http://target/good", new.uri)
    }

    @Test
    fun `don't rewrite a resource that does not match the target namespace`() {
        val old = createDefaultModel().createResource("http://source/good")
        val new = old.rewrite("http://other/", "http://target/")
        assertEquals("http://source/good", new.uri)
    }

    @Test
    fun `rewrite a property that matches the target namespace`() {
        val old = createDefaultModel().createProperty("http://source/good")
        val new = old.rewrite("http://source/", "http://target/")
        assertEquals("http://target/good", new.uri)
    }

    @Test
    fun `don't rewrite a property that does not match the target namespace`() {
        val old = createDefaultModel().createProperty("http://source/good")
        val new = old.rewrite("http://other/", "http://target/")
        assertEquals("http://source/good", new.uri)
    }

    @Test
    fun `keep bnodes as subject unaffected`() {
        val old = createDefaultModel().createResource()
        val new = old.rewrite("http://other/", "http://target/")
        assertEquals(old, new)
    }

    @Test
    fun `plain literals are unaffected`() {
        val old = createDefaultModel().createLiteral("good")
        val new = old.rewrite("http://other/", "http://target/")
        assertEquals("good", new.lexicalForm)
        assertEquals(old.datatype, new.datatype)
    }


    @Test
    fun `rewrite datatypes of typed literals that matches the target namespace`() {
        val old = createDefaultModel().createTypedLiteral("good", "http://source/good")
        val new = old.rewrite("http://source/", "http://target/")
        assertEquals("good", new.lexicalForm)
        assertEquals("http://target/good", new.datatype.uri)
    }

    @Test
    fun `don't rewrite datatypes of typed literals that does not match the target namespace`() {
        val old = createDefaultModel().createTypedLiteral("good", "http://source/good")
        val new = old.rewrite("http://other/", "http://target/")
        assertEquals("good", new.lexicalForm)
        assertEquals("http://source/good", new.datatype.uri)
    }

    @Test
    fun `rewrite a simple model`() {
        val result = aSimpleModel().rewrite("http://source/", "http://target/")
        assertTrue(result.contains(result.createResource("http://target/JohnSmith"), VCARD.FN, "John Smith"))
    }

}