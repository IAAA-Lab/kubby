package es.iaaa.kubby.datasource

import org.apache.jena.rdf.model.*


class RewriterDataSource(
    private val dataSource: DataSource,
    private val targetNamespace: String
) : DataSource {

    override fun describe(namespace: String, localId: String) =
        rewrite(namespace, dataSource.describe(targetNamespace, localId))

    internal fun rewrite(namespace: String, original: Model): Model {
        val result = ModelFactory.createDefaultModel()
        result.setNsPrefixes(original.nsPrefixMap.mapValues { (_, uri) -> rewrite(namespace, uri) })
        original.listStatements().forEach { stmt ->
            val s = rewriteResource(stmt.subject, namespace, result)
            val p = rewriteProperty(stmt.predicate, namespace, result)
            val o = rewriteRDFNode(stmt.`object`, namespace, result)
            result.add(s, p, o)
        }
        return result
    }

    internal fun rewriteResource(resource: Resource, namespace: String, model: Model) =
        with(resource) {
            if (uri?.startsWith(targetNamespace) == true) {
                model.createResource(rewrite(namespace, uri))
            } else {
                this
            }
        }

    internal fun rewriteProperty(property: Property, namespace: String, model: Model) =
        with(property) {
            if (uri?.startsWith(targetNamespace) == true) {
                model.createProperty(rewrite(namespace, uri))
            } else {
                this
            }
        }

    internal fun rewriteLiteral(literal: Literal, namespace: String, model: Model) =
        with(literal) {
            if (datatypeURI?.startsWith(targetNamespace) == true) {
                model.createTypedLiteral(lexicalForm, rewrite(namespace, datatypeURI))
            } else {
                this
            }
        }

    internal fun rewriteRDFNode(obj: RDFNode, namespace: String, model: Model) =
        when (obj) {
            is Resource -> rewriteResource(obj, namespace, model)
            is Literal -> rewriteLiteral(obj, namespace, model)
            else -> obj
        }

    internal fun rewrite(namespace: String, uri: String) = if (uri.startsWith((targetNamespace))) {
        namespace + uri.substring(namespace.length)
    } else {
        uri
    }

    override fun close() {
        dataSource.close()
    }

}