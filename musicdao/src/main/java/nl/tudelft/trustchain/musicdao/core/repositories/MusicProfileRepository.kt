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


        return block
    }


    // For some reason, this function causes the updates to the local database to be lost
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun refreshCache() {
        val musicLikeBlocks = musicProfileBlockRepository.getAll()

        // Each block contains the latest liked songs for a user, update the database accordingly
        for (block in musicLikeBlocks) {
            Log.d("MusicProfile", "Refreshing cache for block: ${block.publicKey}")
            val userPublicKey = block.publicKey
            val likedSongs = block.likedSongs
            val localLikedSongs =
                (database.dao.getAllLikedSongsByUser(userPublicKey).firstOrNull() ?: emptyList()).toSet()
            val remoteLikedSongs = likedSongs.toSet()

            // Find songs to add
            val songsToAdd = remoteLikedSongs - localLikedSongs.map { it.songName }.toSet()

            for (song in songsToAdd) {
                Log.d("MusicProfile", "Adding liked song: $song for user: $userPublicKey")
                database.dao.addLikedSong(
                    userPublicKey = userPublicKey,
                    songName = song,
                    protocolVersion = PROTOCOL_VERSION
                )
            }

            // Find songs to remove
            val songsToRemove = localLikedSongs.map { it.songName }.toSet() - remoteLikedSongs

            for (song in songsToRemove) {
                Log.d("MusicProfile", "Removing liked song: $song for user: $userPublicKey")
                database.dao.removeLikedSong(
                    userPublicKey = userPublicKey,
                    songName = song
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
