package net.osomahe.whitenoise247.db

import com.couchbase.lite.MutableDocument
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class Noise(val start: ZonedDateTime?, val end: ZonedDateTime?) {

    fun toDocument(): MutableDocument {
        val document = MutableDocument()
        start?.let { document.setString("start", it.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)) }
        end?.let { document.setString("end", it.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)) }
        return document
    }
}
