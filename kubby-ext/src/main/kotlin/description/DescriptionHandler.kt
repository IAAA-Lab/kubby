package es.iaaa.kubby.description

import es.iaaa.kubby.config.*
import io.ktor.config.ApplicationConfig
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.shared.PrefixMapping
import org.apache.jena.shared.impl.PrefixMappingImpl
import java.util.*

typealias ContentNode = Map<String, Any>

class DescriptionHandler(val config: ApplicationConfig) {

    fun contentOf(resource: Resource, data: String): ContentNode =
        mapOf(
            "projectName" to config.projectName,
            "projectHomepage" to config.projectHomepage,
            "uri" to resource.uri,
            "title" to (resource.getTitle(config.defaultLanguage, config) ?: ""),
            "comment" to (resource.getComment(config.defaultLanguage, config) ?: ""),
            "image" to (resource.getImageURL(config) ?: ""),
            "properties" to propertiesOf(resource),
            "showLabels" to false,
            "rdfLink" to data,
            "rdfFormat" to "application/ld+json"
        )

    private fun propertiesOf(resource: Resource): List<ContentNode> {
        val direct = resource.listProperties().toList()
            .filter { it.`object`.let { obj -> obj.isLiteral || obj.isURIResource } }
            .groupBy(
                { it.predicate },
                { it.`object`.let { obj -> if (obj.isLiteral) literalOf(obj as Literal) else resourceOf(obj as Resource) } })
            .map { (property, nodes) -> propertyOf(property, false, nodes) }
        val inverse = resource.model.listStatements(null, null, resource).toList()
            .filter { it.subject.isURIResource }
            .groupBy({ it.predicate }, { resourceOf(it.subject) })
            .map { (property, nodes) -> propertyOf(property, true, nodes) }

        val result = direct.toMutableList()
        result.addAll(inverse)
        return result
    }

    private fun propertyOf(
        property: Resource,
        isInverse: Boolean,
        values: List<ContentNode>
    ): ContentNode =
        mapOf(
            "property" to property,
            "Inverse" to isInverse,
            "SimpleValues" to values,
            "LocalName" to property.localName,
            "hasOnlySimpleValues" to true,
            "isURI" to true,
            "Prefix" to (URIPrefixer(property, property.model).prefix ?: ""),
            "hasPrefix" to URIPrefixer(property, property.model).hasPrefix,
            "URI" to property.uri,
            "label" to (property.getLabel(config.defaultLanguage, config) ?: ""),
            "InverseLabel" to (property.getLabel(config.defaultLanguage, config) ?: ""),
            "BrowsableURL" to property.uri
            )

    private fun resourceOf(resource: Resource): ContentNode = mapOf(
        "URI" to resource.uri,
        "LocalName" to (URIPrefixer(resource, resource.model).localName ?: ""),
        "isURI" to true,
        "Prefix" to (URIPrefixer(resource, resource.model).prefix ?: ""),
        "hasPrefix" to URIPrefixer(resource, resource.model).hasPrefix,
        "BrowsableURL" to resource.uri
    )
    private fun literalOf(literal: Literal): ContentNode = mapOf(
        "LiteralLexicalForm" to literal.lexicalForm,
        "DatatypeLabel" to literal.datatypeURI,
        "LiteralLanguage" to literal.language,
        "isLiteral" to true,
        "Prefix" to (URIPrefixer(literal.datatypeURI, literal.model).prefix ?: ""),
        "hasPrefix" to URIPrefixer(literal.datatypeURI, literal.model).hasPrefix
    )

}

fun Resource.getValuesFromMultipleProperties(properties: Collection<Property>) =
    properties.flatMap {
        this.listProperties(it).toList().map { stmt -> stmt.`object` }
    }

private fun getBestLanguageMatch(nodes: Collection<RDFNode>, lang: String?): Literal? {
    val literals = nodes.filter { it.isLiteral }.map { it.asLiteral() }
    return literals.find { lang.isNullOrEmpty() || lang == it.language } ?: literals.firstOrNull()
}

fun Resource.getLabel(lang: String?, props: ApplicationConfig): Literal? {
    val candidates = getValuesFromMultipleProperties(props.labelProperties)
    return getBestLanguageMatch(candidates, lang)
}


fun Resource.getComment(lang: String?, props: ApplicationConfig): String? {
    val candidates = getValuesFromMultipleProperties(props.commentProperties)
    return getBestLanguageMatch(candidates, lang)?.let { toSentenceCase(it.lexicalForm, it.language)}
}

fun Resource.getImageURL(props: ApplicationConfig): String? = getValuesFromMultipleProperties(props.imageProperties)
    .firstOrNull { it.isURIResource }?.asResource()?.uri



private fun toSentenceCase(s: String?, lang: String): String? = s?.trim()?.let { if (it == "") null else it.capitalize() }


private fun Resource.getPrefixes(props: ApplicationConfig): PrefixMapping {
    val prefixes = PrefixMappingImpl()
    prefixes.setNsPrefixes(this.model)
    props.usePrefixes.forEach { prefix, uri -> prefixes.setNsPrefix(prefix, uri) }
    return prefixes
}

fun Resource.getTitle(lang: String?, props: ApplicationConfig): String? {
    fun extractTitle(): String? {
        val match = getPrefixes(props).nsPrefixMap.toList().find { uri.startsWith(it.second) }
        return if (match != null) uri.substring(match.second.length) else null
    }
    if (!this.isResource) return null
    val literal = getLabel(lang, props)
    val label = (literal?.lexicalForm ?: extractTitle()).preferNull
        ?: literal?.language.preferNull
    return label.toTitleCase(lang, props)
}

private val camelCaseBoundaryPattern =
    "(?<=(\\p{javaLowerCase}|\\p{javaUpperCase})\\p{javaLowerCase})(?=\\p{javaUpperCase})".toRegex()

private val wordPattern = "[ \t\r\n-]+".toRegex()

fun String?.toTitleCase(lang: String?, props: ApplicationConfig): String? {
    if (this == null) return null

    val uncapitalizedWords = props.list("uncapitalized-words", lang ?: props.defaultLanguage)

    val str = camelCaseBoundaryPattern.replace(this, " ")
    return if (lang != null) {
        toTitleCase(str, Locale.forLanguageTag(lang), uncapitalizedWords)
    } else {
        toTitleCase(str, uncapitalizedWords)
    }
}

// TODO test
fun ApplicationConfig.list(key: String, lang: String) =
    propertyOrNull("$ROOT_KEY.language-data.$lang.$key")?.getList() ?: emptyList()

private fun toTitleCase(
    str: String,
    uncapitalizedWords: List<String>
): String {
    return wordPattern.split(str)
        .filter { !it.isEmpty() }
        .map { it.toLowerCase() }
        .fold("") { acc, word ->
            "$acc " + if (acc.isEmpty() || !uncapitalizedWords.contains(word)) {
                word.substring(0, 1).toUpperCase() + word.substring(1)
            } else {
                word
            }
        }
        .trimStart()
}

private fun toTitleCase(
    str: String,
    locale: Locale,
    uncapitalizedWords: List<String>
): String {
    return wordPattern.split(str)
        .filter { !it.isEmpty() }
        .map { it.toLowerCase(locale) }
        .fold("") { acc, word ->
            "$acc " + if (acc.isEmpty() || !uncapitalizedWords.contains(word)) {
                word.substring(0, 1).toUpperCase(locale) + word.substring(1)
            } else {
                word
            }
        }
        .trimStart()
}




val String?.preferNull: String?
    get() = if (this == null || isBlank()) null else this
