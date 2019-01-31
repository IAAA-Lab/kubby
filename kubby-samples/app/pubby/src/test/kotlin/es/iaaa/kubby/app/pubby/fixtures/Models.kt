package es.iaaa.kubby.app.pubby.fixtures

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS

object Models {
    fun aSimpleModel(uri: String): Model {
        val m = ModelFactory.createDefaultModel()
        val ns = "http://schema.org/"
        val person = m.createResource(ns + "Person")
        m.setNsPrefix("schema", "http://schema.org/")
        m.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
        m.createResource(uri).let {
            m.add(it, m.createProperty(ns, "projectName"), "Jane Doe")
            m.add(it, RDF.type, person)
            m.add(it, RDFS.seeAlso, m.createResource("http://www.ex.com/janedoe/moreinfo"))
            m.add(it, m.createProperty(ns, "url"), "http://www.janedoe.com")
            m.add(it, m.createProperty(ns, "jobTitle"), "Professor")
        }
        return m
    }

}