package es.iaaa.kubby.app.pubby.rest.api

import es.iaaa.kubby.config.defaultLanguage
import es.iaaa.kubby.description.URIPrefixer
import es.iaaa.kubby.description.getLabel
import io.ktor.config.ApplicationConfig
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource

/**
 * Property DTO.
 */
data class PropertyDto(
    val property: Property,
    val inverse: Boolean,
    val values: List<PropertyValueDto>,
    val localName: String,
    val prefix: String,
    val hasPrefix: Boolean,
    val uri: String,
    val label: String,
    val inverseLabel: String,
    val url: String
)

/**
 * [Resource] to list of [PropertyDto] mapper.
 */
fun Resource.toListContentNodeDto(config: ApplicationConfig): List<PropertyDto> {
    val direct = listProperties().toList()
        .filter { it.`object`.isNode }
        .groupBy({ it.predicate }) { it.`object`.toPropertyValueDto()}
        .map { (property, nodes) -> property.toPropertyDto(false, nodes, config) }
    val inverse = model.listStatements(null, null, this).toList()
        .filter { it.subject.isURIResource }
        .groupBy({ it.predicate }) { it.subject.toValueResourceDto() }
        .map { (property, nodes) -> property.toPropertyDto(true, nodes, config) }
    val result = direct.toMutableList()
    result.addAll(inverse)
    return result
}

/**
 * [Property] to [PropertyDto] mapper.
 */
fun Property.toPropertyDto(isInverse: Boolean, values: List<PropertyValueDto>, config: ApplicationConfig): PropertyDto {
    val prefixer = URIPrefixer(this, model)
    return PropertyDto(
        property = this,
        inverse = isInverse,
        values = values,
        localName = localName,
        prefix = prefixer.prefix ?: "",
        hasPrefix = prefixer.hasPrefix,
        uri = uri,
        label = getLabel(config.defaultLanguage, config)?.lexicalForm ?: "",
        inverseLabel = getLabel(config.defaultLanguage, config)?.lexicalForm ?: "",
        url = uri
    )
}

/**
 * Test if a node is a valid [PropertyValueDto]
 */
val RDFNode.isNode: Boolean get() = isLiteral || isURIResource
