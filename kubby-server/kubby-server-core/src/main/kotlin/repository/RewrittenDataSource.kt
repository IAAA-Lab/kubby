package es.iaaa.kubby.repository

import org.apache.jena.rdf.model.*
import org.apache.jena.vocabulary.OWL

/**
 * Wraps a data source and applies a rewrite.
 *
 * The result is a data source that contains the same data as the original,
 * but with all IRIs starting with [target] are replaced with
 * IRIs starting with a different namespace.
 *
 * @property dataSource the wrapped data source.
 * @property target the namespace to be rewritten.
 * @property addSameAs add a `owl:sameAs` statement to indicate that
 * the rewritten and the original IRIs identify the same entity.
 */
class RewrittenDataSource(
    private val dataSource: DataSource,
    private val target: String,
    private val addSameAs: Boolean = false
) : DataSource {

    /**
     * Describe a resource identified by [namespace] and [localId] by
     * querying the [dataSource] for the resource identified by [target] and [localId]
     * and then retuning a result that contains the same data but with all IRIs
     * starting with [target] replaced with IRIs starting with [namespace].
     *
     * @param namespace the namespace of the resource.
     * @param localId the local identifier of the resource.
     * @return a rewritten response.
     */
    override fun describe(namespace: String, localId: String) =
        rewrite(namespace, dataSource.describe(target, localId))
            .addSameAsStatement(namespace, localId)

    private fun Model.addSameAsStatement(namespace: String, localId: String): Model {
        if (addSameAs && !isEmpty && target != namespace) {
            val original = getResource(target + localId)
            val rewritten = getResource(namespace + localId)
            rewritten.addProperty(OWL.sameAs, original)
            addNsIfUndefined("owl", OWL.NS)
        }
        return this
    }

    internal fun rewrite(newNamespace: String, original: Model): Model {
        val result = ModelFactory.createDefaultModel()
        result.setNsPrefixes(original.nsPrefixMap.mapValues { (_, uri) -> rewrite(newNamespace, uri) })
        original.listStatements().forEach { stmt ->
            val s = rewriteResource(stmt.subject, newNamespace, result)
            val p = rewriteProperty(stmt.predicate, newNamespace, result)
            val o = rewriteRDFNode(stmt.`object`, newNamespace, result)
            result.add(s, p, o)
        }
        return result
    }

    internal fun rewriteResource(resource: Resource, namespace: String, model: Model) =
        with(resource) {
            if (uri?.startsWith(target) == true) {
                model.createResource(rewrite(namespace, uri))
            } else {
                this
            }
        }

    internal fun rewriteProperty(property: Property, namespace: String, model: Model) =
        with(property) {
            if (uri?.startsWith(target) == true) {
                model.createProperty(rewrite(namespace, uri))
            } else {
                this
            }
        }

    internal fun rewriteLiteral(literal: Literal, namespace: String, model: Model) =
        with(literal) {
            if (datatypeURI?.startsWith(target) == true) {
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

    internal fun rewrite(namespace: String, uri: String) =
        if (uri.startsWith(target)) {
            namespace + uri.substring(target.length)
        } else {
            uri
        }

    override fun close() {
        dataSource.close()
    }
}

fun Model.addNsIfUndefined(prefix: String, uri: String) {
    if (this.getNsURIPrefix(uri) != null) return
    if (this.getNsPrefixURI(prefix) != null) return
    this.setNsPrefix(prefix, uri)
}