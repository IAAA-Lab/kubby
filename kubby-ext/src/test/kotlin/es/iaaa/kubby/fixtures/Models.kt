package es.iaaa.kubby.fixtures

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.VCARD

object Models {
    fun aSimpleModel(): Model {
        val model = ModelFactory.createDefaultModel()
        val johnSmith = model.createResource("http://source/JohnSmith")
        johnSmith.addProperty(VCARD.FN, "John Smith")
        return model
    }
}