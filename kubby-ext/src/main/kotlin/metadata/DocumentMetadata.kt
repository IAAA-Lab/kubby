package es.iaaa.kubby.metadata

import es.iaaa.kubby.config.defaultLanguage
import es.iaaa.kubby.config.text
import es.iaaa.kubby.content.ContentKeys.pageId
import es.iaaa.kubby.content.ContentKeys.resourceId
import es.iaaa.kubby.model.addNsIfUndefined
import es.iaaa.kubby.description.getTitle
import io.ktor.config.ApplicationConfig
import io.ktor.util.Attributes
import org.apache.jena.rdf.model.Model
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.RDFS

class DocumentMetadata : MetadataAugmenter {

    override fun augment(model: Model, attributes: Attributes, props: ApplicationConfig) {
        val resourceId = attributes.getOrNull(resourceId)
        val pageId = attributes.getOrNull(pageId)

        if (resourceId != null && pageId != null) {
            val topic = model.getResource(resourceId)
            val document = model.getResource(pageId)
            val title = topic.getTitle(props.defaultLanguage, props) ?: props.text(
                "metadata-document-label-anon",
                props.defaultLanguage
            )

            model.addNsIfUndefined("foaf", FOAF.NS)
            model.addNsIfUndefined("rdfs", RDFS.uri)

            document.addProperty(FOAF.primaryTopic, topic)
            document.addProperty(
                RDFS.label,
                props.text("metadata-document-label", props.defaultLanguage).format(title)
            )
        }
    }
}


fun Metadata.Configuration.documentMetadata() {
    register(DocumentMetadata())
}

