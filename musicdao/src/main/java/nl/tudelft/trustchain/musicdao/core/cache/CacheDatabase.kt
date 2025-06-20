package nl.tudelft.trustchain.musicdao.core.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.ArtistEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicLikeEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicTagEntity
import nl.tudelft.trustchain.musicdao.core.cache.parser.Converters

@Database(
    entities = [AlbumEntity::class, MusicLikeEntity::class, MusicTagEntity::class, ArtistEntity::class],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CacheDatabase : RoomDatabase() {
    abstract val dao: CacheDao
}
