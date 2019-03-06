package es.iaaa.kubby.domain

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
//    fun toEntity() =
//        Entity(ModelFactory.createDefaultModel().createResource(uri))
}