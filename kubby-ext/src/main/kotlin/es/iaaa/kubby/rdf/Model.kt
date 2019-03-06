package es.iaaa.kubby.rdf

import es.iaaa.kubby.text.replacePrefix
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.*
import org.apache.jena.rdf.model.ModelFactory.createDefaultModel
import org.apache.jena.riot.system.PrefixMap
import java.util.*
import java.util.stream.Collectors

/**
 * Rewrites the [RDFNode] from the [old] namespace to the [new] namespace.
 */
fun RDFNode.rewrite(old: String, new: String): RDFNode =
    when (this) {
        is Resource -> rewrite(old, new)
        is Literal -> rewrite(old, new)
        else -> this
    }

/**
 * Get distinct values of the [Resource] used in [properties].
 */
fun Resource.findAllDistinctObjectsFrom(properties: Collection<Property>) =
    properties.flatMap { listProperties(it).toList().map { stmt -> stmt.`object` } }.distinct()

/**
 * Get a representative label of the [Resource] from [properties] in a [language] if available.
 */
fun Resource.findBestLiteral(properties: Collection<Property>, language: String?) =
    findAllDistinctObjectsFrom(properties).firstBestLanguageMatchOrNull(language)

/**
 * Guess a name for the [Resource].
 */
fun Resource.getName(properties: Collection<Property>, language: String?): String =
    findBestLiteral(properties, language)?.lexicalForm ?: localName

/**
 * Returns the namespace that matches with this [Resource].
 */
fun Resource.namespace() = model.uris().firstOrNull { uri.startsWith(it) } ?: ""

/**
 * Returns the local name of this [Resource] regardless the underlying implementation of [Resource.localName].
 */
fun Resource.localName() = namespace().length.let { uri.substring(it) }

/**
 * Test if this [Resource] has a prefix.
 */
fun Resource.hasPrefix() = uri != localName()

/**
 * Returns the prefix for this resource if any.
 */
fun Resource.prefix(): String? = model.getNsURIPrefix(namespace())

/**
 * Rewrites the [Resource] from the [old] namespace to the [new] namespace.
 */
fun Resource.rewrite(old: String, new: String): Resource =
    if (uri?.startsWith(old) == true) {
        model.createResource(uri.replacePrefix(old, new))
    } else {
        this
    }


/**
 * Formats a representation of the lexical form of a [Literal] as capitalized if available.
 */
fun Literal.formattedLexicalForm() = lexicalForm?.trim()?.capitalize()

/**
 * Rewrites the [Literal] from the [old] namespace to the [new] namespace.
 */
fun Literal.rewrite(old: String, new: String): Literal =
    if (datatypeURI?.startsWith(old) == true) {
        model.createTypedLiteral(lexicalForm, datatypeURI.replacePrefix(old, new))
    } else {
        this
    }

/**
 * Get the best [language] match of  list of RDF nodes.
 */
fun List<RDFNode>.firstBestLanguageMatchOrNull(language: String?) =
    with(toLiteralList()) { firstOrNull { it.language == language } ?: firstOrNull() }

/**
 * Get the uri of the first URI resource of a list of RDF nodes.
 */
fun List<RDFNode>.firstUriOrNull() = firstOrNull { it.isURIResource }?.asResource()?.uri

/**
 * Return the sublist of literals.
 */
fun List<RDFNode>.toLiteralList() = filter { it.isLiteral }.map { it.asLiteral() }

/**
 * Add the map of [prefix] to [uri] if undefined.
 */
fun Model.addNsIfUndefined(prefix: String, uri: String): Model {
    if (getNsURIPrefix(uri) == null && getNsPrefixURI(prefix) == null) setNsPrefix(prefix, uri)
    return this
}

/**
 * Add the map of [prefixes].
 */
fun Model.addNsIfUndefined(prefixes: Map<String, String>): Model {
    prefixes.forEach { prefix, uri -> addNsIfUndefined(prefix, uri) }
    return this
}


/**
 * Rewrites the [Model] from the [old] namespace to the [new] namespace.
 */
fun Model.rewrite(old: String, new: String): Model =
    createDefaultModel().let { model ->
        model.setNsPrefixes(nsPrefixMap.mapValues { (_, uri) -> uri.replacePrefix(old, new) })
        listStatements().forEach { stmt ->
            model.add(
                stmt.subject.rewrite(old, new),
                stmt.predicate.rewrite(old, new) as Property,
                stmt.`object`.rewrite(old, new)
            )
        }
        model
    }

/**
 * Merges this [Model] with [other] model.
 */
fun Model.merge(other: Model): Model {
    add(other)
    other.nsPrefixMap.forEach { prefix, uri -> setNsPrefix(prefix, uri) }
    return this
}

/**
 * Ask this model with a ASK [query].
 */
infix fun Model.ask(query: String) = QueryExecutionFactory.create(QueryFactory.create(query), this).execAsk()

/**
 * JSON-LD context holder derived from [prefixes].
 */
class JsonLDContext(prefixes: PrefixMap) {

    private val cxt = TreeMap<String, String>()

    init {
        putPrefixes(prefixes)
    }

    private fun putPrefixes(prefixes: PrefixMap) {
        prefixes.mapping.forEach { (key, value) -> putPrefix(key, value.toASCIIString()) }
    }

    private fun putPrefix(key: String, value: String) {
        cxt[if (key.isEmpty()) "@vocab" else key] = value
    }

    /**
     * Returns a representation in JSON.
     */
    fun toJson(): String {
        return cxt.entries.stream()
            .map { entry -> "\"${entry.key}\": \"${entry.value}\"" }
            .collect(Collectors.joining(", ", "{", "}"))
    }
}