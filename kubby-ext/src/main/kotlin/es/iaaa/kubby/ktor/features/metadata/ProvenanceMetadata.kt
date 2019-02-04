package es.iaaa.kubby.ktor.features.metadata

import es.iaaa.kubby.config.ProjectDescription
import es.iaaa.kubby.ktor.features.Metadata
import es.iaaa.kubby.ktor.features.MetadataProcesor
import es.iaaa.kubby.rdf.addNsIfUndefined
import es.iaaa.kubby.rest.api.ContentContext
import es.iaaa.kubby.rest.api.DataContentContext
import es.iaaa.kubby.vocabulary.PROV
import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS

/**
 * Provenance metadata description.
 */
class ProvenanceMetadata : MetadataProcesor {

    override fun process(model: Model, context: ContentContext, projectDescription: ProjectDescription) {
        val date = model.createTypedLiteral(context.time)
        val document = model.getResource(if (context is DataContentContext) context.data else context.page)
        val agent = model.createResource()
        val activity = model.createResource()

        model.apply {
            addNsIfUndefined("prov", PROV.uri)
            addNsIfUndefined("rdf", RDF.uri)
            addNsIfUndefined("rdfs", RDFS.uri)
            agent.apply {
                addProperty(RDF.type, PROV.SoftwareAgent)
                addLiteral(RDFS.label, projectDescription.softwareName)
            }
            document.apply {
                addProperty(RDF.type, PROV.Entity)
                addProperty(PROV.wasGeneratedBy, activity)
                addLiteral(PROV.generatedAtTime, date)
            }
            activity.apply {
                addProperty(RDF.type, PROV.Activity)
                addProperty(PROV.generated, document)
                addProperty(PROV.wasAssociatedWith, agent)
            }
        }
    }
}

/**
 * Registers [ProvenanceMetadata] processor.
 */
fun Metadata.Configuration.provenance() {
    register(ProvenanceMetadata())
}

