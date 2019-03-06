package es.iaaa.kubby.domain

import es.iaaa.kubby.text.replacePrefix

/**
 * Response entity.
 */
interface Entity {
    val uri: String
    val attributions: List<String>
    val isEmpty: Boolean
    fun toGraphModel(): Any
    fun merge(other: Entity): Entity
    fun merge(other: Map<String, String>): Entity

    /**
     * Rewrites an Entity from a [old] namespace to a [new] namespace.
     */
    fun rewrite(old: String, new: String): Entity

    /**
     * Adds owl:sameAs statement between this [Entity] and the [other] uri.
     */
    fun addSameAs(other: String): Entity
}

class NullEntity(override val uri: String) : Entity {
    override val attributions: List<String> = emptyList()

    override val isEmpty: Boolean = true

    override fun toGraphModel(): Any = throw NoGraphModelException("$uri is NullEntity")

    override fun merge(other: Entity) =
        if (uri == other.uri) other else throw IncompatibleEntityException("Other uri ${other.uri} is different from $uri")

    override fun merge(other: Map<String, String>) = throw ImmutableEntityException("$uri is NullEntity")

    override fun rewrite(old: String, new: String) = NullEntity(uri.replacePrefix(old, new))

    override fun addSameAs(other: String) = throw ImmutableEntityException("$uri is NullEntity")
}


class IncompatibleEntityException(msg: String) : Exception(msg)

class ImmutableEntityException(msg: String) : Exception(msg)

class NoGraphModelException(msg: String) : Exception(msg)