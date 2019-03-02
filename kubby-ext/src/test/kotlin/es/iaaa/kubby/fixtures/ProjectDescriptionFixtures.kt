package es.iaaa.kubby.fixtures

val defaultLabelProperties = listOf(
    "http://www.w3.org/2000/01/rdf-schema#label",
    "http://purl.org/dc/elements/1.1/title",
    "http://purl.org/dc/terms/title",
    "http://xmlns.com/foaf/0.1/projectName",
    "http://schema.org/projectName"
)

val defaultCommentProperties = listOf(
    "http://www.w3.org/2000/01/rdf-schema#comment",
    "http://purl.org/dc/elements/1.1/description",
    "http://purl.org/dc/terms/description"
)

val defaultImageProperties = listOf(
    "http://xmlns.com/foaf/0.1/depiction"
)

val defaultUsePrefixes = mapOf(
    "xs" to "http://www.w3.org/2001/XMLSchema#",
    "rdfs" to "http://www.w3.org/2000/01/rdf-schema#",
    "rdf" to "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "dc" to "http://purl.org/dc/elements/1.1/",
    "dcterms" to "http://purl.org/dc/terms/",
    "foaf" to "http://xmlns.com/foaf/0.1/",
    "schema" to "http://schema.org/"
)