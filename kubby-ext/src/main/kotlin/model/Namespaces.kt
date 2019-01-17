package es.iaaa.kubby.model

import org.apache.jena.rdf.model.Model

fun Model.addNsIfUndefined(prefix: String, uri: String) {
    if (this.getNsURIPrefix(uri) != null) return
    if (this.getNsPrefixURI(prefix) != null) return
    this.setNsPrefix(prefix, uri)
}