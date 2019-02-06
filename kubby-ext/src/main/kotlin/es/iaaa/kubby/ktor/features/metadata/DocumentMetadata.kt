package es.iaaa.kubby.ktor.features.metadata

import es.iaaa.kubby.config.ProjectDescription
import es.iaaa.kubby.ktor.features.Metadata
import es.iaaa.kubby.ktor.features.MetadataProcesor
import es.iaaa.kubby.rdf.addNsIfUndefined
import es.iaaa.kubby.rdf.getName
import es.iaaa.kubby.rest.api.ContentContext
import es.iaaa.kubby.rest.api.DataContentContext
import es.iaaa.kubby.text.toTitleCase
import org.apache.jena.rdf.model.Model
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.RDFS

/**
 * Document metadata description.
 */
class DocumentMetadata : MetadataProcesor {

    override fun process(model: Model, context: ContentContext, projectDescription: ProjectDescription) {
        val topic = model.getResource(context.resource.uri)
        val title: String =
            topic.getName(projectDescription.labelProperties, projectDescription.defaultLanguage)
                .toTitleCase(projectDescription.getLanguageList("uncapitalized-words"))
                .ifEmpty { projectDescription.getLanguageValue("metadata-document-label-anon") }
        model.apply {
            addNsIfUndefined("foaf", FOAF.NS)
            addNsIfUndefined("rdfs", RDFS.uri)
            val uri = if (context is DataContentContext) context.dataUri else context.pageUri
            getResource(uri).apply {
                addProperty(FOAF.primaryTopic, topic)
                addProperty(
                    RDFS.label,
                    projectDescription.getLanguageValue("metadata-document-label").format(title) // LABEL should be generic "Description of ..."
                )
            }
        }
    }
}

/**
 * Registers [DocumentMetadata] processor.
 */
fun Metadata.Configuration.document() {
    register(DocumentMetadata())
}

