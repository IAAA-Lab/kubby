package es.iaaa.kubby.ktor.features.metadata

import es.iaaa.kubby.config.ProjectDescription
import es.iaaa.kubby.ktor.features.Metadata
import es.iaaa.kubby.ktor.features.MetadataProcesor
import es.iaaa.kubby.rdf.addNsIfUndefined
import es.iaaa.kubby.rest.api.RequestContext
import org.apache.jena.rdf.model.Model
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.RDF

/**
 * Provenance metadata description.
 */
class ProvenanceMetadata : MetadataProcesor {

    override fun process(model: Model, context: RequestContext, projectDescription: ProjectDescription) {
        val date = model.createTypedLiteral(context.time)
        val document = model.getResource(context.page)
        val agent = model.createResource()
        val prov = "http://www.w3.org/ns/prov#"
        val activity = model.createResource()

        model.apply {
            addNsIfUndefined("prov", prov)
            addNsIfUndefined("rdf", RDF.uri)
            addNsIfUndefined("foaf", FOAF.NS)
            agent.apply {
                addProperty(RDF.type, createResource(prov + "SoftwareAgent"))
                addProperty(RDF.type, createResource(prov + "Agent"))
                addLiteral(FOAF.name, projectDescription.softwareName)
            }
            document.apply {
                addProperty(RDF.type, createResource(prov + "Entity"))
                addProperty(createProperty(prov, "wasGeneratedBy"), activity)
                addLiteral(createProperty(prov, "generatedAtTime"), date)
            }
            activity.apply {
                addProperty(RDF.type, createResource(prov + "Activity"))
                addProperty(createProperty(prov, "generated"), document)
                addProperty(createProperty(prov, "wasAssociatedWith"), agent)
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

