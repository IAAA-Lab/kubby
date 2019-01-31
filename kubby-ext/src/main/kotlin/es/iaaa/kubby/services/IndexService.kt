package es.iaaa.kubby.services

/**
 * Access to the properties of the index resource.
 */
interface IndexService {
    /**
     * Returns the local part of the index resource.
     */
    fun indexLocalPart(): String?
}