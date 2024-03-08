package net.osomahe.whitenoise247.db

import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Ordering
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DbFacade private constructor() {

    private val database: Database = Database("whitenoise247")
    private val collectionNoise = database.createCollection("noise")

    fun createNewNoise(): String {
        val noise = Noise(ZonedDateTime.now(), null)
        val document = noise.toDocument()
        collectionNoise.save(document)
        return document.id
    }

    fun endNoise(noiseId: String) {
        collectionNoise.getDocument(noiseId)?.let {
            collectionNoise.save(
                it.toMutable().setString("end", ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
            )
        }
    }

    fun getNoises(): List<Noise> {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collectionNoise))
            .orderBy(Ordering.property("start").descending())

        val list = query.execute().map {
            Noise(
                it.getString("start")?.let { ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) },
                it.getString("end")?.let { ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) }
            )
        }
        return listOf(Noise(ZonedDateTime.now().minusDays(1), ZonedDateTime.now()))
    }

    companion object {

        @Volatile
        private var instance: DbFacade? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: DbFacade().also { instance = it }
            }
    }
}