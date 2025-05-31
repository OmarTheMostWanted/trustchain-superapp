package nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike

import android.annotation.SuppressLint
import android.util.Log
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.musicdao.core.ipv8.MusicCommunity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants
import javax.inject.Inject

class MusicLikeBlockRepository
@Inject
constructor(
    private val musicCommunity: MusicCommunity,
    private val musicLikeBlockValidator: MusicLikeBlockValidator
) {

    public val myPeerPublicKey = musicCommunity.myPeer.publicKey.keyToBin().toHex()

    suspend fun getOrCrawl(songId: String): List<MusicLikeBlock>? {
        val block = get(songId)
        Log.d("MusicDao", "getOrCrawl 1: $block $songId")
        return if (block.isNotEmpty()) {
            block
        } else {
            crawl(songId)
            get(songId)
        }
    }

    /**
     * Get the likes for a given song
     */
    fun get(songId: String): List<MusicLikeBlock> {
        return musicCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE)
            .filter { musicLikeBlockValidator.validateTransaction(it.transaction) }
            .map { MusicLikeBlock.fromTrustChainTransaction(it.transaction) }
            .filter { it.likedSongs.contains(songId) }
    }

    fun getAllKnownSongLikes(): List<MusicLikeBlock> {
        val songLikes =
            musicCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE)
                .filter { musicLikeBlockValidator.validateTransaction(it.transaction) }
                .map {MusicLikeBlock.fromTrustChainTransaction(it.transaction)}

        return songLikes
    }

    @SuppressLint("NewApi")
    private suspend fun crawl(publicKey: String) {
        val key = musicCommunity.publicKeyStringToByteArray(publicKey)
        val peer = musicCommunity.network.getVerifiedByPublicKeyBin(key)
        Log.d("MusicDao", "crawl: peer is? $peer")
        Log.d("MusicDao", "all peers")
        musicCommunity.network.verifiedPeers.forEach {
            Log.d("MusicDao", "peer: ${it.publicKey.keyToBin().toHex()}")
        }

        if (peer != null) {
            Log.d("MusicDao", "crawl: peer found $peer, crawling")
            musicCommunity.crawlChain(peer = peer)
        } else {
            val randomPeers = musicCommunity.network.getRandomPeers(10) - musicCommunity.myPeer
            Log.d("MusicDao", "crawl: crawling random peers ${randomPeers.size}")
            try {
                randomPeers.forEach {
                    musicCommunity.sendCrawlRequest(it, key, LongRange(-1, -1))
                }
            } catch (e: Exception) {
                return
            }
        }
    }

    fun create(create: CreateMusicLikeBlock): TrustChainBlock? {

        val allLikedSongs = musicCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE)
            .filter { musicLikeBlockValidator.validateTransaction(it.transaction) }
            .map { MusicLikeBlock.fromTrustChainTransaction(it.transaction) }
            .flatMap { it.likedSongs }
            .toMutableSet()

//        allLikedSongs.add(create.likedMusicId) // Add the new liked song
        // Update the list of liked songs
        if (create.likedMusicId in allLikedSongs) {
            allLikedSongs.remove(create.likedMusicId) // Unlike
        } else {
            allLikedSongs.add(create.likedMusicId) // Like
        }

        val transaction = MusicLikeBlock.Companion.toTransaction(
            MusicLikeBlock(
                publicKey = myPeerPublicKey,
                name = myPeerPublicKey,
                likedSongs = allLikedSongs.toList(),
                protocolVersion = Constants.PROTOCOL_VERSION
            )
        )

        if (!musicLikeBlockValidator.validateTransaction(transaction)) {
            return null
        }

        return musicCommunity.createProposalBlock(
            blockType = MusicLikeBlock.BLOCK_TYPE,
            transaction = transaction,
            publicKey = musicCommunity.myPeer.publicKey.keyToBin()
        )
    }

    fun toBlock(trustChainBlock: TrustChainBlock): MusicLikeBlock {
        return MusicLikeBlock.fromTrustChainTransaction(trustChainBlock.transaction)
    }

    companion object {
        data class CreateMusicLikeBlock(
            val likedMusicId: String
        )
    }
}
