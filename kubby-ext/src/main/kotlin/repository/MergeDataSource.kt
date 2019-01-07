package es.iaaa.kubby.repository

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory

class MergeDataSource(val datasources: List<DataSource>) : DataSource {
    override fun qname(uri: String) = datasources
        .map { it.qname(uri) }
        .find { it.namespaceURI != NULL_NS_URI }
        ?: QName(localPart = uri)

    override fun describe(qname: QName) = datasources
        .fold(ModelFactory.createDefaultModel()) { r, ds -> r.merge(ds.describe(qname))}

    override fun close() {
        datasources.forEach { it.close() }
    }
}

fun Model.merge(source: Model): Model {
    add(source)
    source.nsPrefixMap.forEach { prefix, uri -> setNsPrefix(prefix, uri)}
    return this
}