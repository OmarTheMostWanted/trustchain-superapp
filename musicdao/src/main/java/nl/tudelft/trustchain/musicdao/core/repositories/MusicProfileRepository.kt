package nl.tudelft.trustchain.musicdao.core.repositories

import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants.PROTOCOL_VERSION
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfile
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfileBlockRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import javax.inject.Inject


class MusicProfileRepository @Inject
constructor(
    private val database: CacheDatabase,
    private val musicProfileBlockRepository: MusicProfileBlockRepository
) {
    suspend fun getLikes(): List<MusicLike> {
        return database.dao.getCurrentVersionLikes().map { it.toMusicLike() }
    }

    suspend fun toggleLike(track: Song): MusicProfile? {

        val id = MusicLike.musicLikeIdFromSong(track)

        val likedSongs_debug =
            database.dao.getAllLikedSongsByUser(musicProfileBlockRepository.myPeerPublicKey)
                .firstOrNull() ?: emptyList()
        val likedSongsList_debug = likedSongs_debug.map { it.songName }.toList()

        Log.d("MusicProfile", "Liked songs before toggle: $likedSongsList_debug")


        val isLiked =
            database.dao.isSongLikedByUser(musicProfileBlockRepository.myPeerPublicKey, id)
                .firstOrNull() ?: false
        if (!isLiked) {
            Log.d("MusicProfile", "Liking Song: ${track.title}")
            database.dao.addLikedSong(
                userPublicKey = musicProfileBlockRepository.myPeerPublicKey,
                songName = id,
                protocolVersion = PROTOCOL_VERSION
            )
        } else {
            Log.d("MusicProfile", "Removing like for song: ${track.title}")
            database.dao.removeLikedSong(
                userPublicKey = musicProfileBlockRepository.myPeerPublicKey,
                songName = id
            )

            // debug check if song was removed
            val likedSongsAfterRemoval =
                database.dao.getAllLikedSongsByUser(musicProfileBlockRepository.myPeerPublicKey)
                    .firstOrNull() ?: emptyList()

            val likedSongsListAfterRemoval = likedSongsAfterRemoval.map { it.songName }.toList()

            Log.d("MusicProfile", "Liked songs after removal: $likedSongsListAfterRemoval")

        }
        val likedSongs =
            database.dao.getAllLikedSongsByUser(musicProfileBlockRepository.myPeerPublicKey)
                .firstOrNull() ?: emptyList()
        val likedSongsList = likedSongs.map { it.songName }.toList()
        val companion = MusicProfileBlockRepository.Companion.MusicProfileCompanion(likedSongsList)
        val block = musicProfileBlockRepository.create(companion)
            ?.let { MusicProfile.fromTrustChainTransaction(it.transaction) }

//        refreshCache()

        return block
    }


    // For some reason, this function causes the updates to the local database to be lost
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun refreshCache() {
        val musicLikeBlocks = musicProfileBlockRepository.getAll()
        val remoteLikedSongs = musicLikeBlocks.flatMap { it.likedSongs }.toSet()
        val localLikedSongs = database.dao.getCurrentVersionLikes().map { it.songName }.toSet()

        // Find songs that need to be removed from the local database
        val songsToRemove = localLikedSongs - remoteLikedSongs

        // Remove songs that are no longer liked remotely
        songsToRemove.forEach { songName ->
            database.dao.removeLikedSong(
                userPublicKey = musicProfileBlockRepository.myPeerPublicKey,
                songName = songName
            )
        }

        // Update the local database with the latest music likes
        musicLikeBlocks.forEach {
            it.likedSongs.forEach { songName ->
                database.dao.addLikedSong(
                    userPublicKey = it.publicKey,
                    songName = songName,
                    protocolVersion = it.protocolVersion
                )
            }
        }
    }
//
//    private suspend fun insertBlock(block: MusicProfile) {
//        database.dao.insertMusicLike(
//            MusicLikeEntity(
//                publicKey = block.publicKey,
//                likedSongs = block.likedSongs.joinToString(","), // Serialize list to JSON string
//                name = block.name,
//                protocolVersion = block.protocolVersion,
//                id = block.publicKey // Use publicKey as the unique ID for the block
//            )
//        )
//    }

//    private fun getDatabaseIdFromBlock(block: MusicLikeBlock): String {
//        return "${block.name}_${block.likedMusicId}"
//    }

    fun isSongLikedByMe(track: Song): Flow<Boolean> {
        return database.dao.isSongLikedByUser(
            musicProfileBlockRepository.myPeerPublicKey,
            MusicLike.musicLikeIdFromSong(track)
        )
    }

}
