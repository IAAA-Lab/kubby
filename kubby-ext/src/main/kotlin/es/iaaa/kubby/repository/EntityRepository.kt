package es.iaaa.kubby.repository

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import java.io.Closeable


/**
 * Response entity.
 */
class Entity(
    val resource: Resource,
    val attribution: List<String> = emptyList()
) {

    /**
     * Ensure the correcteness of this response.
     */
    fun normalize() = if (resource.model.isEmpty) Entity(resource) else this

    /**
     * Merges two responses into a new one.
     */
    fun merge(other: Entity): Entity {
        val model = ModelFactory.createDefaultModel()
        model.add(resource.model)
        model.add(other.resource.model)
        return Entity(
            resource = model.getResource(resource.uri),
            attribution = attribution.union(other.attribution).toList()
        )
    }

    val isEmpty: Boolean get() = resource.model.isEmpty
}



/**
 * A facade interface for the repositories.
 *
 * This allows to provide several implementations.
 */
interface EntityRepository : Closeable {

    /**
     * Generate the [EntityId] representation for a [uri] in this repository.
     */
    fun getId(uri: String): EntityId

    /**
     * Get the [Model] describing one resource based on the [id].
     */
    fun findOne(id: EntityId): Entity
}

/**
 * Qualified identifier composed of a [namespace] and a [localPart].
 */
data class EntityId(
    val namespace: String = "",
    val localPart: String
) {
    val qualified = namespace != ""
    val uri = "$namespace$localPart"
    override fun toString() = "{$namespace}$localPart"
    fun toEntity() = Entity(ModelFactory.createDefaultModel().createResource(uri))
}

