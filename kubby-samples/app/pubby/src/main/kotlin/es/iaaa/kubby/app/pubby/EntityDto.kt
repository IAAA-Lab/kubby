package es.iaaa.kubby.app.pubby

import es.iaaa.kubby.config.ProjectDescription
import es.iaaa.kubby.rdf.*
import es.iaaa.kubby.repository.Entity
import es.iaaa.kubby.text.toTitleCase

/**
 * Entity DTO.
 */
data class EntityDto(
    val projectName: String?,
    val projectHomepage: String?,
    val uri: String? = null,
    val title: String? = null,
    val comment: String? = null,
    val image: String? = null,
    val properties: List<PropertyDto>? = null,
    val attribution: List<String> = emptyList(),
    val showLabels: Boolean,
    val rdfLink: String? = null,
    val rdfFormat: String? = null
)

/**
 * [Entity] to [EntityDto] mapper.
 */
fun Entity?.toEntityDto(config: ProjectDescription, data: String) =
    if (this != null)
        EntityDto(
            projectName = config.projectName,
            projectHomepage = config.projectHomepage,
            uri = resource.uri,
            title = resource.getName(config.labelProperties, config.defaultLanguage)
                .toTitleCase(config.getLanguageList("uncapitalized-words")),
            comment = resource.findBestLiteral(config.commentProperties, config.defaultLanguage)?.formattedLexicalForm() ?: "",
            image = resource.findAllDistinctObjectsFrom(config.imageProperties).firstUriOrNull() ?: "",
            properties = resource.toListContentNodeDto(config),
            attribution = attribution,
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
        "rdfFormat" to rdfFormat,
        "attribution" to attribution
    )
        .filterValues { it != null }
        .mapValues { it.value as Any }
