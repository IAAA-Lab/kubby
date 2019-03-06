package es.iaaa.kubby.domain.impl

import es.iaaa.kubby.domain.Entity
import es.iaaa.kubby.domain.IncompatibleEntityException
import es.iaaa.kubby.rdf.addNsIfUndefined
import es.iaaa.kubby.text.replacePrefix
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory

data class MergedEntityImpl(
    override val uri: String,
    val entities: List<Entity>,
    val prefixes: Map<String, String> = emptyMap()
) : Entity {
    override fun rewrite(old: String, new: String) = copy(
        uri = uri.replacePrefix(old, new),
        entities = entities.map { it.rewrite(old, new) }
    )

    override val attributions: List<String>
        get() = entities.flatMap { it.attributions }.distinct()
    override val isEmpty: Boolean
        get() = entities.isEmpty()

    override fun toGraphModel() = if (entities.isEmpty()) {
        ModelFactory.createDefaultModel()
    } else {
        val graphs = entities.map { it.toGraphModel() }.filterIsInstance(Model::class.java)
        if (graphs.size != entities.size) throw IncompatibleEntityException("Unknown graph model")
        graphs.fold(ModelFactory.createDefaultModel()) { acc, any -> acc.add(any) }.addNsIfUndefined(prefixes)
    }

    override fun merge(other: Entity): Entity {
        if (uri != other.uri) throw IncompatibleEntityException("Other uri ${other.uri} is different from $uri")
        return if (other.isEmpty) this else MergedEntityImpl(uri, entities + other)
    }

    override fun merge(other: Map<String, String>) = copy(prefixes = prefixes.plus(other))

    override fun addSameAs(other: String): Entity = merge(
        sameAsGranule(
            uri,
            other
        )
    )
}