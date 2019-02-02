package es.iaaa.kubby.fixtures

import es.iaaa.kubby.rdf.addNsIfUndefined
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.VCARD

object Models {
    fun aSimpleModel(): Model {
        val model = ModelFactory.createDefaultModel()
        val johnSmith = model.createResource("http://source/JohnSmith")
        johnSmith.addProperty(VCARD.FN, "John Smith")
        model.addNsIfUndefined("vcard", VCARD.uri)
        return model
    }

    fun aSimpleResource() = aSimpleModel().getResource("http://source/JohnSmith")

    fun anEmptyResource() = ModelFactory.createDefaultModel().createResource()
}
