package nl.tudelft.trustchain.musicdao.core.ipv8

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfile
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfileBlockValidator
import javax.inject.Inject

class MusicProfileGossiper
@Inject
constructor(
    private val musicCommunity: MusicCommunity,
    private val musicProfileBlockValidator: MusicProfileBlockValidator
) {
    fun startGossip(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            while (coroutineScope.isActive) {
//                Log.d("MusicProfile", "Starting to gossip music likes")
                gossip()
                delay(Config.DELAY)
            }
        }
    }

    private fun gossip() {
        val randomPeer = pickRandomPeer()
        if (randomPeer == null) {
            Log.d("MusicProfile", "No peers available for gossiping music profile")
            return
        }

        val validTrustChainBlocks =
            musicCommunity.database.getBlocksWithType(MusicProfile.BLOCK_TYPE)
                .filter { musicProfileBlockValidator.validateTransaction(it.transaction) }

        Log.d(
            "MusicProfile",
            "Gossiping music profile blocks, found ${validTrustChainBlocks.size} valid blocks"
        )

        val groupedByPublicKey = validTrustChainBlocks.groupBy { it.publicKey.toHex() }

        val newestBlocks = groupedByPublicKey.map { entry ->
            entry.value.maxByOrNull { it.timestamp } ?: entry.value.first()
        }

        newestBlocks.shuffled().take(Config.BLOCKS).forEach { block ->
            musicCommunity.sendBlock(block, randomPeer)
        }

        Log.d(
            "MusicProfile",
            "Gossiped ${newestBlocks.size} unique music profile blocks to peer ${
                randomPeer.publicKey.keyToBin().toHex()
            }"
        )

//        val musicProfileBlocks = musicCommunity.database.getBlocksWithType(MusicProfile.BLOCK_TYPE)
//            .filter { musicProfileBlockValidator.validateTransaction(it.transaction) }
//            .shuffled()
//            .take(Config.BLOCKS)
//        musicProfileBlocks.forEach {
//            musicCommunity.sendBlock(it, randomPeer)
    }


    object Config {
        const val BLOCKS = 10
        const val DELAY = 5_000L
    }

    private fun pickRandomPeer(): Peer? {
        val peers = musicCommunity.getPeers()
        if (peers.isEmpty()) return null
        return peers.random()
    }
}
