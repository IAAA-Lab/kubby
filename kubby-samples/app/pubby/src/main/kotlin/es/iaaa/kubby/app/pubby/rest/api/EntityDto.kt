package es.iaaa.kubby.app.pubby.rest.api

import es.iaaa.kubby.config.defaultLanguage
import es.iaaa.kubby.config.projectHomepage
import es.iaaa.kubby.config.projectName
import es.iaaa.kubby.description.getComment
import es.iaaa.kubby.description.getImageURL
import es.iaaa.kubby.description.getTitle
import io.ktor.config.ApplicationConfig
import org.apache.jena.rdf.model.Resource

/**
 * Entity DTO.
 */
data class EntityDto(
    val projectName: String,
    val projectHomepage: String,
    val uri: String,
    val title: String,
    val comment: String,
    val image: String,
    val properties: List<PropertyDto>,
    val showLabels: Boolean,
    val rdfLink: String,
    val rdfFormat: String
)

/**
 * [Resource] to [EntityDto] mapper.
 */
fun Resource.toEntityDto(config: ApplicationConfig, data: String) =
        EntityDto(
            projectName = config.projectName,
            projectHomepage = config.projectHomepage,
            uri = uri,
            title = getTitle(config.defaultLanguage, config) ?: "",
            comment = getComment(config.defaultLanguage, config) ?: "",
            image = getImageURL(config) ?: "",
            properties = toListContentNodeDto(config),
            showLabels = false,
            rdfLink = data,
            rdfFormat = "application/ld+json"
        )

/**
 * [EntityDto] to [Map] mapper.
 */
fun EntityDto.toMap(): Map<String,Any> =
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
