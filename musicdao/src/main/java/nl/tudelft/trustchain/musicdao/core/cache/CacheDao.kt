package nl.tudelft.trustchain.musicdao.core.cache

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicLikeEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicTagEntity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants
import nl.tudelft.trustchain.musicdao.core.repositories.model.TagCount

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

    @Query("SELECT * FROM MusicLikeEntity WHERE protocolVersion = :version")
    suspend fun getCurrentVersionLikes(version: String = Constants.PROTOCOL_VERSION): List<MusicLikeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLikedSong(musicLike: MusicLikeEntity)

<<<<<<< HEAD
    @Query("SELECT EXISTS(SELECT 1 FROM MusicLikeEntity WHERE likedMusicId = :songId AND name = :myId)")
    fun isSongLikedByMe(
        songId: String,
        myId: String
    ): Flow<Boolean>
=======
    suspend fun addLikedSong(userPublicKey: String, songName: String, protocolVersion: String = Constants.PROTOCOL_VERSION) {
        val musicLikeEntity = MusicLikeEntity(
            publicKey = userPublicKey,
            songName = songName,
            protocolVersion = protocolVersion
        )
        addLikedSong(musicLikeEntity)
    }

    @Query("DELETE FROM MusicLikeEntity WHERE publicKey = :userPublicKey AND songName = :songName")
    suspend fun removeLikedSong(userPublicKey: String, songName: String)

    @Query("SELECT * FROM MusicLikeEntity WHERE publicKey = :userPublicKey")
    fun getAllLikedSongsByUser(userPublicKey: String): Flow<List<MusicLikeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM MusicLikeEntity WHERE publicKey = :userPublicKey AND songName = :songName)")
    fun isSongLikedByUser(userPublicKey: String, songName: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTag(tag: MusicTagEntity)

    @Query("DELETE FROM MusicTagEntity WHERE publicKey = :publicKey AND songName = :songName AND tag = :tag")
    suspend fun removeTag(publicKey: String, songName: String, tag: String)

    @Query("SELECT tag, COUNT(*) as count FROM MusicTagEntity WHERE songName = :songName GROUP BY tag ORDER BY count DESC LIMIT 3")
    suspend fun getTopTagsForSong(songName: String): List<TagCount>

    @Query("SELECT tag FROM MusicTagEntity WHERE publicKey = :publicKey AND songName = :songName")
    suspend fun getUserTagsForSong(publicKey: String, songName: String): List<String>

    @Query("SELECT * FROM MusicTagEntity WHERE publicKey = :publicKey")
    suspend fun getUserTagsForAllSongs(publicKey: String): List<MusicTagEntity>

    @Query("SELECT * FROM MusicLikeEntity WHERE protocolVersion = :protocolVersion")
    fun getAllLikesFlow(protocolVersion: String = Constants.PROTOCOL_VERSION): Flow<List<MusicLikeEntity>>

//    @Query("SELECT * FROM MusicLikeEntity")
//    suspend fun getAllMusicLikes(): List<MusicLikeEntity>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertMusicLike(musicLike: MusicLikeEntity)
//
//    @Query("SELECT EXISTS(SELECT 1 FROM MusicLikeEntity WHERE likedSongs LIKE '%' || :songId || '%' AND publicKey = :myId)")
//    fun isSongLikedByMe(songId: String, myId: String): Flow<Boolean>
//
//    @Query("SELECT EXISTS(SELECT 1 FROM MusicLikeEntity WHERE publicKey = :myId)")
//    fun getAllSongsLikedByMe(myId: String): Flow<List<String>>

//    @Query("SELECT EXISTS(SELECT 1 FROM MusicLikeEntity WHERE likedMusicId = :songId AND name = :myId)")
//    fun isSongLikedByMe(songId: String, myId: String): Flow<Boolean>
>>>>>>> master
}
