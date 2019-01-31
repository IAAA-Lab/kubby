package es.iaaa.kubby.app.pubby.rest.api

import es.iaaa.kubby.config.ProjectDescription
import es.iaaa.kubby.rdf.*
import es.iaaa.kubby.text.toTitleCase
import org.apache.jena.rdf.model.Resource

/**
 * Entity DTO.
 */
data class EntityDto(
    val projectName: String,
    val projectHomepage: String,
    val uri: String? = null,
    val title: String? = null,
    val comment: String? = null,
    val image: String? = null,
    val properties: List<PropertyDto>? = null,
    val showLabels: Boolean,
    val rdfLink: String? = null,
    val rdfFormat: String? = null
)

/**
 * [Resource] to [EntityDto] mapper.
 */
fun Resource?.toEntityDto(config: ProjectDescription, data: String) =
    if (this != null)
        EntityDto(
            projectName = config.projectName,
            projectHomepage = config.projectHomepage,
            uri = uri,
            title = getName(
                config.labelProperties,
                config.defaultLanguage
            ).toTitleCase(config.getLanguageList("uncapitalized-words"))
                ?: "",
            comment = findBestLiteral(config.commentProperties, config.defaultLanguage)?.formattedLexicalForm() ?: "",
            image = findAllDistinctObjectsFrom(config.imageProperties).firstUriOrNull() ?: "",
            properties = toListContentNodeDto(config),
            showLabels = false,
            rdfLink = data,
            rdfFormat = "application/ld+toJson"
        )
    else
        EntityDto(
            projectName = config.projectName,
            projectHomepage = config.projectHomepage,
            showLabels = false
        )

/**
 * [EntityDto] to [Map] mapper.
 */
fun EntityDto.toMap(): Map<String, Any> =
    mapOf(
        "projectName" to projectName,
        "projectHomepage" to projectHomepage,
        "uri" to uri,
        "title" to title,
        "comment" to comment,
        "image" to image,
        "properties" to properties,
        "showLabels" to showLabels,
        "rdfLink" to rdfLink,
        "rdfFormat" to rdfFormat
    )
        .filterValues { it != null }
        .mapValues { it.value as Any }
