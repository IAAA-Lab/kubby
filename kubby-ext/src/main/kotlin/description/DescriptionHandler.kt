package es.iaaa.kubby.description

import description.URIPrefixer
import es.iaaa.kubby.config.defaultLanguage
import es.iaaa.kubby.config.projectHomepage
import es.iaaa.kubby.config.projectName
import es.iaaa.kubby.util.getComment
import es.iaaa.kubby.util.getImageURL
import es.iaaa.kubby.util.getLabel
import es.iaaa.kubby.util.getTitle
import io.ktor.config.ApplicationConfig
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Resource

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
