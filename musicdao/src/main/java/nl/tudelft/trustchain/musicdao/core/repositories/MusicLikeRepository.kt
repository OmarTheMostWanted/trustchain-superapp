package nl.tudelft.trustchain.musicdao.core.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.DelicateCoroutinesApi
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicLikeEntity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlock
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlockRepository
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.releasePublish.ReleasePublishBlock
import nl.tudelft.trustchain.musicdao.core.repositories.model.Album
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike
import nl.tudelft.trustchain.musicdao.core.torrent.TorrentEngine
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
         name: String,
         likedMusicId: String
    ): Boolean {
        // Create and publish the Trustchain block.
        val block =
            musicLikeBlockRepository.create(
                create = MusicLikeBlockRepository.Companion.CreateMusicLikeBlock(name, likedMusicId)
            )?.let { MusicLikeBlock.fromTrustChainTransaction(it.transaction) }

        // If successful, we optimistically add it to our local cache.
        block?.let {
            database.dao.insertMusicLike(
                MusicLikeEntity(
                    publicKey = it.publicKey,
                    likedMusicId = it.likedMusicId,
                    name = it.name,
                    protocolVersion = it.protocolVersion,
                    id = "${it.name}_${it.likedMusicId}"
                )
            )
        }

        return block != null
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun refreshCache() {
        val musicLikeBlocks = musicLikeBlockRepository.getAllKnownSongLikes()

        musicLikeBlocks.forEach {
            database.dao.insertMusicLike(
                MusicLikeEntity(
                    publicKey = it.publicKey,
                    likedMusicId = it.likedMusicId,
                    name = it.name,
                    protocolVersion = it.protocolVersion,
                    id = "${it.name}_${it.likedMusicId}"
                )
            )
        }
    }
}
