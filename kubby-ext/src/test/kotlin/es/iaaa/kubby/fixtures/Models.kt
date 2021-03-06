package es.iaaa.kubby.fixtures

import es.iaaa.kubby.domain.Entity
import es.iaaa.kubby.domain.impl.ResourceEntityImpl
import es.iaaa.kubby.rdf.addNsIfUndefined
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.VCARD

object Models {

    fun johnSmithModel(): Model {
        val model = ModelFactory.createDefaultModel()
        val johnSmith = model.createResource("http://source/JohnSmith")
        val marySmith = model.createResource("http://source/MarySmith")
        johnSmith.apply {
            addProperty(VCARD.FN, "John Smith")
            addProperty(DC_11.relation, marySmith)
        }
        model.addNsIfUndefined("vcard", VCARD.uri)
        return model
    }

    fun emptyEntity(uri: String) = ResourceEntityImpl(uri, ModelFactory.createDefaultModel())

    fun johnSmith() = ResourceEntityImpl("http://source/JohnSmith", johnSmithModel())

    fun marySmithModel(): Model {
        val model = ModelFactory.createDefaultModel()
        val marySmith = model.createResource("http://source/MarySmith")
        val johnSmith = model.createResource("http://source/JohnSmith")
        marySmith.apply {
            addProperty(VCARD.FN, "Mary Smith")
            addProperty(DC_11.relation, johnSmith)
        }
        model.addNsIfUndefined("vcard", VCARD.uri)
        return model
    }

    fun marySmith() = ResourceEntityImpl("http://source/MarySmith", marySmithModel())

    fun marySmithAboutJohnSmith() = ResourceEntityImpl("http://source/JohnSmith", marySmithModel())

    fun anEmptyEntity(): Entity {
        val resource = ModelFactory.createDefaultModel().createResource("urn:empty")
        return ResourceEntityImpl(resource.uri, resource.model)
    }
}
