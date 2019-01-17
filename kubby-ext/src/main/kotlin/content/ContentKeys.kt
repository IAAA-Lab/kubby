package es.iaaa.kubby.content

import io.ktor.util.AttributeKey
import java.util.*

object ContentKeys {
    val resourceId = AttributeKey<String>("resourceId")
    val pageId = AttributeKey<String>("pageId")
    val aboutId = AttributeKey<String>("aboutId")
    val timeId = AttributeKey<Calendar>("timeId")
}