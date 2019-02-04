package es.iaaa.kubby.vocabulary

import org.apache.jena.rdf.model.ModelFactory

/**
 * Vocabulary definitions from http://www.w3.org/ns/prov.owl
 */
object PROV {

    private val M_MODEL = ModelFactory.createDefaultModel()

    val uri = "http://www.w3.org/ns/prov#"

    val NAMESPACE = M_MODEL.createResource(uri)
    val VERSION_INFO = "Working Group Note version 2013-04-30"

    val actedOnBehalfOf = M_MODEL.createProperty("http://www.w3.org/ns/prov#actedOnBehalfOf")
    val activity = M_MODEL.createProperty("http://www.w3.org/ns/prov#activity")
    val agent = M_MODEL.createProperty("http://www.w3.org/ns/prov#agent")
    val alternateOf = M_MODEL.createProperty("http://www.w3.org/ns/prov#alternateOf")
    val asInBundle = M_MODEL.createProperty("http://www.w3.org/ns/prov#asInBundle")
    val atLocation = M_MODEL.createProperty("http://www.w3.org/ns/prov#atLocation")
    val derivedByInsertionFrom = M_MODEL.createProperty("http://www.w3.org/ns/prov#derivedByInsertionFrom")
    val derivedByRemovalFrom = M_MODEL.createProperty("http://www.w3.org/ns/prov#derivedByRemovalFrom")
    val describesService = M_MODEL.createProperty("http://www.w3.org/ns/prov#describesService")
    val dictionary = M_MODEL.createProperty("http://www.w3.org/ns/prov#dictionary")
    val entity = M_MODEL.createProperty("http://www.w3.org/ns/prov#entity")
    val generated = M_MODEL.createProperty("http://www.w3.org/ns/prov#generated")
    val hadActivity = M_MODEL.createProperty("http://www.w3.org/ns/prov#hadActivity")
    val hadDictionaryMember = M_MODEL.createProperty("http://www.w3.org/ns/prov#hadDictionaryMember")
    val hadGeneration = M_MODEL.createProperty("http://www.w3.org/ns/prov#hadGeneration")
    val hadMember = M_MODEL.createProperty("http://www.w3.org/ns/prov#hadMember")
    val hadPlan = M_MODEL.createProperty("http://www.w3.org/ns/prov#hadPlan")
    val hadPrimarySource = M_MODEL.createProperty("http://www.w3.org/ns/prov#hadPrimarySource")
    val hadRole = M_MODEL.createProperty("http://www.w3.org/ns/prov#hadRole")
    val hadUsage = M_MODEL.createProperty("http://www.w3.org/ns/prov#hadUsage")
    val has_anchor = M_MODEL.createProperty("http://www.w3.org/ns/prov#has_anchor")
    val has_provenance = M_MODEL.createProperty("http://www.w3.org/ns/prov#has_provenance")
    val has_query_service = M_MODEL.createProperty("http://www.w3.org/ns/prov#has_query_service")
    val influenced = M_MODEL.createProperty("http://www.w3.org/ns/prov#influenced")
    val influencer = M_MODEL.createProperty("http://www.w3.org/ns/prov#influencer")
    val insertedKeyEntityPair = M_MODEL.createProperty("http://www.w3.org/ns/prov#insertedKeyEntityPair")
    val invalidated = M_MODEL.createProperty("http://www.w3.org/ns/prov#invalidated")
    val mentionOf = M_MODEL.createProperty("http://www.w3.org/ns/prov#mentionOf")
    val pairEntity = M_MODEL.createProperty("http://www.w3.org/ns/prov#pairEntity")
    val pingback = M_MODEL.createProperty("http://www.w3.org/ns/prov#pingback")
    val qualifiedAssociation = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedAssociation")
    val qualifiedAttribution = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedAttribution")
    val qualifiedCommunication = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedCommunication")
    val qualifiedDelegation = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedDelegation")
    val qualifiedDerivation = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedDerivation")
    val qualifiedEnd = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedEnd")
    val qualifiedGeneration = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedGeneration")
    val qualifiedInfluence = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedInfluence")
    val qualifiedInsertion = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedInsertion")
    val qualifiedInvalidation = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedInvalidation")
    val qualifiedPrimarySource = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedPrimarySource")
    val qualifiedQuotation = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedQuotation")
    val qualifiedRemoval = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedRemoval")
    val qualifiedRevision = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedRevision")
    val qualifiedStart = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedStart")
    val qualifiedUsage = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedUsage")
    val specializationOf = M_MODEL.createProperty("http://www.w3.org/ns/prov#specializationOf")
    val used = M_MODEL.createProperty("http://www.w3.org/ns/prov#used")
    val wasAssociatedWith = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasAssociatedWith")
    val wasAttributedTo = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasAttributedTo")
    val wasDerivedFrom = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasDerivedFrom")
    val wasEndedBy = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasEndedBy")
    val wasGeneratedBy = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasGeneratedBy")
    val wasInfluencedBy = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasInfluencedBy")
    val wasInformedBy = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasInformedBy")
    val wasInvalidatedBy = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasInvalidatedBy")
    val wasQuotedFrom = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasQuotedFrom")
    val wasRevisionOf = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasRevisionOf")
    val wasStartedBy = M_MODEL.createProperty("http://www.w3.org/ns/prov#wasStartedBy")

    val atTime = M_MODEL.createProperty("http://www.w3.org/ns/prov#atTime")
    val endedAtTime = M_MODEL.createProperty("http://www.w3.org/ns/prov#endedAtTime")
    val generatedAtTime = M_MODEL.createProperty("http://www.w3.org/ns/prov#generatedAtTime")
    val invalidatedAtTime = M_MODEL.createProperty("http://www.w3.org/ns/prov#invalidatedAtTime")
    val pairKey = M_MODEL.createProperty("http://www.w3.org/ns/prov#pairKey")
    val provenanceUriTemplate = M_MODEL.createProperty("http://www.w3.org/ns/prov#provenanceUriTemplate")
    val removedKey = M_MODEL.createProperty("http://www.w3.org/ns/prov#removedKey")
    val startedAtTime = M_MODEL.createProperty("http://www.w3.org/ns/prov#startedAtTime")
    val value = M_MODEL.createProperty("http://www.w3.org/ns/prov#value")

    val aq = M_MODEL.createProperty("http://www.w3.org/ns/prov#aq")
    val category = M_MODEL.createProperty("http://www.w3.org/ns/prov#category")
    val component = M_MODEL.createProperty("http://www.w3.org/ns/prov#component")
    val constraints = M_MODEL.createProperty("http://www.w3.org/ns/prov#constraints")
    val definition = M_MODEL.createProperty("http://www.w3.org/ns/prov#definition")
    val dm = M_MODEL.createProperty("http://www.w3.org/ns/prov#dm")
    val editorialNote = M_MODEL.createProperty("http://www.w3.org/ns/prov#editorialNote")
    val editorsDefinition = M_MODEL.createProperty("http://www.w3.org/ns/prov#editorsDefinition")
    val inverse = M_MODEL.createProperty("http://www.w3.org/ns/prov#inverse")
    val n = M_MODEL.createProperty("http://www.w3.org/ns/prov#n")
    val order = M_MODEL.createProperty("http://www.w3.org/ns/prov#order")
    val qualifiedForm = M_MODEL.createProperty("http://www.w3.org/ns/prov#qualifiedForm")
    val sharesDefinitionWith = M_MODEL.createProperty("http://www.w3.org/ns/prov#sharesDefinitionWith")
    val todo = M_MODEL.createProperty("http://www.w3.org/ns/prov#todo")
    val unqualifiedForm = M_MODEL.createProperty("http://www.w3.org/ns/prov#unqualifiedForm")

    val Accept = M_MODEL.createResource("http://www.w3.org/ns/prov#Accept")
    val Activity = M_MODEL.createResource("http://www.w3.org/ns/prov#Activity")
    val ActivityInfluence = M_MODEL.createResource("http://www.w3.org/ns/prov#ActivityInfluence")
    val Agent = M_MODEL.createResource("http://www.w3.org/ns/prov#Agent")
    val AgentInfluence = M_MODEL.createResource("http://www.w3.org/ns/prov#AgentInfluence")
    val Association = M_MODEL.createResource("http://www.w3.org/ns/prov#Association")
    val Attribution = M_MODEL.createResource("http://www.w3.org/ns/prov#Attribution")
    val Bundle = M_MODEL.createResource("http://www.w3.org/ns/prov#Bundle")
    val Collection = M_MODEL.createResource("http://www.w3.org/ns/prov#Collection")
    val Communication = M_MODEL.createResource("http://www.w3.org/ns/prov#Communication")
    val Contribute = M_MODEL.createResource("http://www.w3.org/ns/prov#Contribute")
    val Contributor = M_MODEL.createResource("http://www.w3.org/ns/prov#Contributor")
    val Copyright = M_MODEL.createResource("http://www.w3.org/ns/prov#Copyright")
    val Create = M_MODEL.createResource("http://www.w3.org/ns/prov#Create")
    val Creator = M_MODEL.createResource("http://www.w3.org/ns/prov#Creator")
    val Delegation = M_MODEL.createResource("http://www.w3.org/ns/prov#Delegation")
    val Derivation = M_MODEL.createResource("http://www.w3.org/ns/prov#Derivation")
    val Dictionary = M_MODEL.createResource("http://www.w3.org/ns/prov#Dictionary")
    val DirectQueryService = M_MODEL.createResource("http://www.w3.org/ns/prov#DirectQueryService")
    val EmptyCollection = M_MODEL.createResource("http://www.w3.org/ns/prov#EmptyCollection")
    val EmptyDictionary = M_MODEL.createResource("http://www.w3.org/ns/prov#EmptyDictionary")
    val End = M_MODEL.createResource("http://www.w3.org/ns/prov#End")
    val Entity = M_MODEL.createResource("http://www.w3.org/ns/prov#Entity")
    val EntityInfluence = M_MODEL.createResource("http://www.w3.org/ns/prov#EntityInfluence")
    val Generation = M_MODEL.createResource("http://www.w3.org/ns/prov#Generation")
    val Influence = M_MODEL.createResource("http://www.w3.org/ns/prov#Influence")
    val Insertion = M_MODEL.createResource("http://www.w3.org/ns/prov#Insertion")
    val InstantaneousEvent = M_MODEL.createResource("http://www.w3.org/ns/prov#InstantaneousEvent")
    val Invalidation = M_MODEL.createResource("http://www.w3.org/ns/prov#Invalidation")
    val KeyEntityPair = M_MODEL.createResource("http://www.w3.org/ns/prov#KeyEntityPair")
    val Location = M_MODEL.createResource("http://www.w3.org/ns/prov#Location")
    val Modify = M_MODEL.createResource("http://www.w3.org/ns/prov#Modify")
    val Organization = M_MODEL.createResource("http://www.w3.org/ns/prov#Organization")
    val Person = M_MODEL.createResource("http://www.w3.org/ns/prov#Person")
    val Plan = M_MODEL.createResource("http://www.w3.org/ns/prov#Plan")
    val PrimarySource = M_MODEL.createResource("http://www.w3.org/ns/prov#PrimarySource")
    val Publish = M_MODEL.createResource("http://www.w3.org/ns/prov#Publish")
    val Publisher = M_MODEL.createResource("http://www.w3.org/ns/prov#Publisher")
    val Quotation = M_MODEL.createResource("http://www.w3.org/ns/prov#Quotation")
    val Removal = M_MODEL.createResource("http://www.w3.org/ns/prov#Removal")
    val Replace = M_MODEL.createResource("http://www.w3.org/ns/prov#Replace")
    val Revision = M_MODEL.createResource("http://www.w3.org/ns/prov#Revision")
    val RightsAssignment = M_MODEL.createResource("http://www.w3.org/ns/prov#RightsAssignment")
    val RightsHolder = M_MODEL.createResource("http://www.w3.org/ns/prov#RightsHolder")
    val Role = M_MODEL.createResource("http://www.w3.org/ns/prov#Role")
    val ServiceDescription = M_MODEL.createResource("http://www.w3.org/ns/prov#ServiceDescription")
    val SoftwareAgent = M_MODEL.createResource("http://www.w3.org/ns/prov#SoftwareAgent")
    val Start = M_MODEL.createResource("http://www.w3.org/ns/prov#Start")
    val Submit = M_MODEL.createResource("http://www.w3.org/ns/prov#Submit")
    val Usage = M_MODEL.createResource("http://www.w3.org/ns/prov#Usage")

}