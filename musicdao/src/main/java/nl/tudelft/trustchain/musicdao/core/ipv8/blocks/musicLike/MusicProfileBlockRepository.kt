package nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike

import android.annotation.SuppressLint
import android.util.Log
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.musicdao.core.ipv8.MusicCommunity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants
import javax.inject.Inject


class MusicProfileBlockRepository
@Inject
constructor(
    private val musicCommunity: MusicCommunity,
    private val musicProfileBlockValidator: MusicProfileBlockValidator,
    ) {

    val myPeerPublicKey = musicCommunity.myPeer.publicKey.keyToBin().toHex()

//    suspend fun getOrCrawl(songId: String): List<MusicProfile>? {
//        val block = get(songId)
//        Log.d("MusicDao", "getOrCrawl 1: $block $songId")
//        return if (block.isNotEmpty()) {
//            block
//        } else {
//            crawl(songId)
//            get(songId)
//        }
//    }

    /**
     * Get the likes for a given song
     */
    fun get(songId: String): List<MusicProfile> {
        return musicCommunity.database.getBlocksWithType(MusicProfile.BLOCK_TYPE)
            .filter { musicProfileBlockValidator.validateTransaction(it.transaction) }
            .map { MusicProfile.fromTrustChainTransaction(it.transaction) }
            .filter { it.likedSongs.contains(songId) }
    }

    fun getAll(): List<MusicProfile> {
        val validTrustChainBlocks =  musicCommunity.database.getBlocksWithType(MusicProfile.BLOCK_TYPE)
            .filter { musicProfileBlockValidator.validateTransaction(it.transaction) }

        //Log How many valid blocks we have
        Log.d("MusicDao", "getAll: Found ${validTrustChainBlocks.size} valid MusicProfile blocks")

        val groupedByPublicKey = validTrustChainBlocks.groupBy { it.publicKey.toHex() }

        // Log how many unique public keys we have
        Log.d("MusicDao", "getAll: Found ${groupedByPublicKey.size} unique public keys in MusicProfile blocks")
        // Print the public keys
        groupedByPublicKey.keys.forEach { publicKey ->
            Log.d("MusicDao", "getAll: Public key: $publicKey")
        }

        val listOfOnlyNestBlocks = groupedByPublicKey.map { entry ->
            entry.value.maxByOrNull { it.timestamp } ?: entry.value.first()
        }

        return listOfOnlyNestBlocks.map { MusicProfile.fromTrustChainTransaction(it.transaction) }

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

    fun create(companion: MusicProfileCompanion): TrustChainBlock? {
        val transaction = MusicProfile.Companion.toTransaction(
            MusicProfile(
                publicKey = myPeerPublicKey,
                likedSongs = companion.allLikedSongs,
                protocolVersion = Constants.PROTOCOL_VERSION
            )
        )
        if (!musicProfileBlockValidator.validateTransaction(transaction)) {
            Log.e("MusicProfile", "create: Invalid transaction for MusicProfile")
            return null
        }
        Log.d("MusicProfile", "create: Creating MusicProfile block with ${companion.allLikedSongs.toString()}")
        return musicCommunity.createProposalBlock(
            blockType = MusicProfile.BLOCK_TYPE,
            transaction = transaction,
            publicKey = musicCommunity.myPeer.publicKey.keyToBin()
        )
    }

    fun toBlock(trustChainBlock: TrustChainBlock): MusicProfile {
        return MusicProfile.fromTrustChainTransaction(trustChainBlock.transaction)
    }

    companion object {
        data class MusicProfileCompanion(
            val allLikedSongs: List<String>
            // tags
        )
    }
}
