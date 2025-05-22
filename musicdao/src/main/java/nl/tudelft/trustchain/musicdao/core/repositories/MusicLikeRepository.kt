package nl.tudelft.trustchain.musicdao.core.repositories

import androidx.lifecycle.map
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicLikeEntity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlock
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlockRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import javax.inject.Inject

class MusicLikeRepository @Inject
constructor(
    private val database: CacheDatabase,
    private val musicLikeBlockRepository: MusicLikeBlockRepository
) {
    suspend fun getLikes(): List<MusicLike> {
        return database.dao.getAllMusicLikes().map { it.toMusicLike() }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun createMusicLike(
         track: Song
    ): MusicLikeBlock? {
        val id = MusicLike.musicLikeIdFromSong(track)
        // Create and publish the Trustchain block.
        val block =
            musicLikeBlockRepository.create(
                create = MusicLikeBlockRepository.Companion.CreateMusicLikeBlock(id)
            )?.let { MusicLikeBlock.fromTrustChainTransaction(it.transaction) }

        // If successful, we optimistically add it to our local cache.
        block?.let {
            insertBlock(it)
        }

        return block
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun refreshCache() {
        val musicLikeBlocks = musicLikeBlockRepository.getAllKnownSongLikes()
        musicLikeBlocks.forEach {
            insertBlock(it)
        }
    }

    private suspend fun insertBlock(block: MusicLikeBlock) {
        database.dao.insertMusicLike(
            MusicLikeEntity(
                publicKey = block.publicKey,
                likedMusicId = block.likedMusicId,
                name = block.name,
                protocolVersion = block.protocolVersion,
                id = getDatabaseIdFromBlock(block)
            )
        )
    }

    private fun getDatabaseIdFromBlock(block: MusicLikeBlock): String {
        return "${block.name}_${block.likedMusicId}"
    }

    fun isSongLikedByMe(track: Song): Flow<Boolean> {
        return database.dao.isSongLikedByMe(MusicLike.musicLikeIdFromSong(track), musicLikeBlockRepository.myPeerPublicKey)
    }
}
