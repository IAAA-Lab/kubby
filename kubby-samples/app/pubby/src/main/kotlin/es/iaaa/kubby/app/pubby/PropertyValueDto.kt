package es.iaaa.kubby.app.pubby

import es.iaaa.kubby.rdf.hasPrefix
import es.iaaa.kubby.rdf.localName
import es.iaaa.kubby.rdf.prefix
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource

/**
 * Root mode of Property Value DTO.
 */
interface PropertyValueDto {
    val literal: Boolean
}

/**
 * [PropertyValueDto] derived from [Resource].
 */
data class ValueResourceDto(
    val uri: String,
    val localName: String,
    val prefix: String?,
    val hasPrefix: Boolean,
    override val literal: Boolean = false
) : PropertyValueDto

/**
 * [PropertyValueDto] derived from [Literal].
 */
data class ValueLiteralDto(
    val lexicalForm: String,
    val datatypeLabel: String,
    val language: String,
    val prefix: String?,
    val hasPrefix: Boolean,
    override val literal: Boolean = true
) : PropertyValueDto


/**
 * [RDFNode] to [PropertyValueDto] or [ValueLiteralDto] mapper.
 */
fun RDFNode.toPropertyValueDto(): PropertyValueDto =
    if (isLiteral) (this as Literal).toValueLiteralDto() else (this as Resource).toValueResourceDto()


/**
 * [Literal] to [ValueLiteralDto] mapper.
 */
fun Literal.toValueLiteralDto(): ValueLiteralDto {
    val dataType = model.createResource(datatypeURI)
    return ValueLiteralDto(
        lexicalForm = lexicalForm,
        language = language,
        datatypeLabel = dataType.localName(),
        prefix = dataType.prefix(),
        hasPrefix = dataType.hasPrefix()
    )
}

/**
 * [Resource] to [ValueResourceDto] mapper.
 */
fun Resource.toValueResourceDto(): ValueResourceDto {
    return ValueResourceDto(
        uri = uri,
        localName = localName(),
        prefix = prefix(),
        hasPrefix = hasPrefix()
    )
}

