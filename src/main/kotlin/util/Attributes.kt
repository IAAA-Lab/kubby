package es.iaaa.kubby.util

import io.ktor.util.AttributeKey
import java.util.*

object AttributeKeys {
    val resourceId = AttributeKey<String>("resourceId")
    val pageId = AttributeKey<String>("pageId")
    val aboutId = AttributeKey<String>("aboutId")
    val timeId = AttributeKey<Calendar>("timeId")
}