package es.iaaa.kubby.domain.impl

import es.iaaa.kubby.domain.Entity
import es.iaaa.kubby.rdf.addNsIfUndefined
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.OWL

fun sameAsGranule(uri: String, other: String): Entity =
    ResourceEntityImpl(
        uri = uri,
        model = with(ModelFactory.createDefaultModel()) {
            add(createResource(uri), OWL.sameAs, createResource(other)).addNsIfUndefined("owl", OWL.NS)
        })


