package es.iaaa.kubby.util

import io.ktor.util.AttributeKey

object AttributeKeys {
    val resourceId = AttributeKey<String>("resourceId")
    val pageId = AttributeKey<String>("pageId")
}