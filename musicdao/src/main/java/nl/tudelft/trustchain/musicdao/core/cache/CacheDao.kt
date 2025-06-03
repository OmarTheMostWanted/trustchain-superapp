package nl.tudelft.trustchain.musicdao.core.cache

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicLikeEntity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(infos: AlbumEntity)

    @Query("DELETE FROM AlbumEntity WHERE id IS :id")
    suspend fun delete(id: String)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: AlbumEntity)

    @Query("SELECT * FROM AlbumEntity")
    suspend fun getAll(): List<AlbumEntity>

    @Query("SELECT * FROM AlbumEntity")
    fun getAllLiveData(): LiveData<List<AlbumEntity>>

    @Query("SELECT * FROM AlbumEntity WHERE id is :id")
    suspend fun get(id: String): AlbumEntity

    @Query("SELECT * FROM AlbumEntity WHERE id is :id")
    fun getLiveData(id: String): LiveData<AlbumEntity>

    @Query("SELECT * FROM AlbumEntity WHERE infoHash is :infoHash")
    suspend fun getFromInfoHash(infoHash: String): List<AlbumEntity>

    @Query("SELECT * FROM AlbumEntity WHERE publisher is :publicKey")
    suspend fun getFromArtist(publicKey: String): List<AlbumEntity>

    @Query("SELECT * FROM AlbumEntity WHERE artist LIKE '%' || :keyword || '%' OR title LIKE '%' || :keyword || '%'")
    suspend fun localSearch(keyword: String): List<AlbumEntity>

    @Query("SELECT * FROM MusicLikeEntity")
    suspend fun getAllMusicLikes(): List<MusicLikeEntity>

    @Query("SELECT * FROM MusicLikeEntity WHERE protocolVersion = :version")
    suspend fun getCurrentVersionLikes(version: String = Constants.PROTOCOL_VERSION): List<MusicLikeEntity>

//    @Query("SELECT * FROM MusicLikeEntity WHERE protocolVersion is :version")
//    suspend fun getCurrentVersionLikes(version: String = Constants.PROTOCOL_VERSION): List<MusicLikeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusicLike(musicLike: MusicLikeEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM MusicLikeEntity WHERE likedSongs LIKE '%' || :songId || '%' AND name = :myId)")
    fun isSongLikedByMe(songId: String, myId: String): Flow<Boolean>

//    @Query("SELECT EXISTS(SELECT 1 FROM MusicLikeEntity WHERE likedMusicId = :songId AND name = :myId)")
//    fun isSongLikedByMe(songId: String, myId: String): Flow<Boolean>
}
