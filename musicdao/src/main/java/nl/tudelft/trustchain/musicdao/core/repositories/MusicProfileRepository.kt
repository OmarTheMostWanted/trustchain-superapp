package nl.tudelft.trustchain.musicdao.core.repositories

import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicTagEntity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants.PROTOCOL_VERSION
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfile
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfileBlockRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import nl.tudelft.trustchain.musicdao.core.repositories.model.TagCount
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

        val allTags = database.dao.getUserTagsForAllSongs(musicProfileBlockRepository.myPeerPublicKey)
            .groupBy { it.songName }
            .mapValues { it.value.map { tag -> tag.tag }.take(3) }

        val companion = MusicProfileBlockRepository.Companion.MusicProfileCompanion(
            allLikedSongs = likedSongsList,
            allTags = allTags
        )

        val block = musicProfileBlockRepository.create(companion)
            ?.let { MusicProfile.fromTrustChainTransaction(it.transaction) }


        return block
    }

    suspend fun toggleTag(song: Song, tag: String): MusicProfile? {
        val songId = MusicLike.musicLikeIdFromSong(song)
        val userKey = musicProfileBlockRepository.myPeerPublicKey

        val currentTags = database.dao.getUserTagsForSong(userKey, songId)
        if (tag in currentTags) {
            database.dao.removeTag(userKey, songId, tag)
        } else if (currentTags.size < 3) {
            database.dao.addTag(
                MusicTagEntity(
                    publicKey = userKey,
                    songName = songId,
                    tag = tag,
                    protocolVersion = PROTOCOL_VERSION
                )
            )
        }

        val likedSongs = database.dao.getAllLikedSongsByUser(userKey)
            .firstOrNull()?.map { it.songName } ?: emptyList()

        val allTags = database.dao.getUserTagsForAllSongs(userKey)
            .groupBy { it.songName }
            .mapValues { entry -> entry.value.map { it.tag }.take(3) }

        val companion = MusicProfileBlockRepository.Companion.MusicProfileCompanion(
            allLikedSongs = likedSongs,
            allTags = allTags
        )

        return musicProfileBlockRepository.create(companion)
            ?.let { MusicProfile.fromTrustChainTransaction(it.transaction) }
    }

    // For some reason, this function causes the updates to the local database to be lost
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun refreshCache() {
        val musicLikeBlocks = musicProfileBlockRepository.getAll()

        // Each block contains the latest liked songs for a user, update the database accordingly
        for (block in musicLikeBlocks) {
            //Log.d("MusicProfile", "Refreshing cache for block: ${block.publicKey}")
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

            val remoteTags = block.tags
            val localTags = database.dao.getUserTagsForAllSongs(userPublicKey)

            val remoteTagTriples = remoteTags.flatMap { (song, tags) ->
                tags.map { tag -> Triple(song, tag, userPublicKey) }
            }.toSet()
            val localTagTriples = localTags.map { Triple(it.songName, it.tag, it.publicKey) }.toSet()

            val tagsToAdd = remoteTagTriples - localTagTriples
            val tagsToRemove = localTagTriples - remoteTagTriples

            for ((song, tag, pubKey) in tagsToAdd) {
                Log.d("MusicProfile", "Inserting tag: $tag for song: $song from user: $userPublicKey")
                database.dao.addTag(
                    MusicTagEntity(pubKey, song, tag, PROTOCOL_VERSION)
                )
            }

            for ((song, tag, pubKey) in tagsToRemove) {
                database.dao.removeTag(pubKey, song, tag)
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

    suspend fun getUserTagsForSong(song: Song): List<String> {
        return database.dao.getUserTagsForSong(
            musicProfileBlockRepository.myPeerPublicKey,
            MusicLike.musicLikeIdFromSong(song)
        )
    }

    suspend fun getTopTagsForSong(song: Song): List<TagCount> {
        return database.dao.getTopTagsForSong(MusicLike.musicLikeIdFromSong(song))
    }


}
