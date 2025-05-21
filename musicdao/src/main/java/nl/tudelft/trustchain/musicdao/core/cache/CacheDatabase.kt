package nl.tudelft.trustchain.musicdao.core.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicLikeEntity
import nl.tudelft.trustchain.musicdao.core.cache.parser.Converters

@Database(
    entities = [AlbumEntity::class, MusicLikeEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CacheDatabase : RoomDatabase() {
    abstract val dao: CacheDao
}

//val MIGRATION_5_6 = object : Migration(4, 5) {
//    override fun migrate(db: SupportSQLiteDatabase) {
//        db.execSQL("""
//            CREATE TABLE IF NOT EXISTS `MusicLikeEntity` (
//                `id` TEXT NOT NULL PRIMARY KEY,
//                `songId` TEXT NOT NULL,
//                `likedAt` INTEGER NOT NULL
//            )
//        """.trimIndent())
//    }
//}
