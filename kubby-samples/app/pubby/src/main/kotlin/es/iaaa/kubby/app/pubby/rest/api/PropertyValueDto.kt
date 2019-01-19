package es.iaaa.kubby.app.pubby.rest.api

import es.iaaa.kubby.description.URIPrefixer
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource

/**
 * Root definition of Property Value DTO.
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
    val prefix: String,
    val hasPrefix: Boolean,
    val url: String,
    override val literal: Boolean = false
) : PropertyValueDto

/**
 * [PropertyValueDto] derived from [Literal].
 */
data class ValueLiteralDto(
    val lexicalForm: String,
    val datatypeLabel: String,
    val language: String,
    val prefix: String,
    val hasPrefix: Boolean,
    override val literal: Boolean = true
) : PropertyValueDto


/**
 * [RDFNode] to [PropertyValueDto] or [ValueLiteralDto] mapper.
 */
fun RDFNode.toPropertyValueDto(): PropertyValueDto =
    if (isLiteral) {
        (this as Literal).toValueLiteralDto()
    } else {
        (this as Resource).toValueResourceDto()
    }

/**
 * [Literal] to [ValueLiteralDto] mapper.
 */
fun Literal.toValueLiteralDto() : ValueLiteralDto {
    val prefixer = URIPrefixer(datatypeURI, model)
    return ValueLiteralDto(
        lexicalForm = lexicalForm,
        datatypeLabel = datatypeURI,
        language = language,
        prefix =  prefixer.prefix ?: "",
        hasPrefix = prefixer.hasPrefix
    )
}

/**
 * [Resource] to [ValueResourceDto] mapper.
 */
fun Resource.toValueResourceDto() : ValueResourceDto {
    val prefixer = URIPrefixer(this, model)
    return ValueResourceDto(
        uri = uri,
        localName = prefixer.localName ?: "",
        prefix =  prefixer.prefix ?: "",
        hasPrefix = prefixer.hasPrefix,
        url = uri
    )
}

