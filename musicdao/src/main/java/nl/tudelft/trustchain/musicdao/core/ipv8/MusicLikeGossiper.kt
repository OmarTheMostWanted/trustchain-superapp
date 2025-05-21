package nl.tudelft.trustchain.musicdao.core.ipv8

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.tudelft.ipv8.Peer
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlock
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlockValidator
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.releasePublish.ReleasePublishBlock
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.releasePublish.ReleasePublishBlockValidator
import javax.inject.Inject

class MusicLikeGossiper
@Inject
constructor(
    private val musicCommunity: MusicCommunity,
    private val musicLikeBlockValidator: MusicLikeBlockValidator
) {
    fun startGossip(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            while (coroutineScope.isActive) {
                Log.d("MusicLike", "Starting to gossip music likes")
                gossip()
                delay(Config.DELAY)
            }
        }
    }

    private fun gossip() {
        val randomPeer = pickRandomPeer()
        val releaseBlocks =
            musicCommunity.database.getBlocksWithType(MusicLikeBlock.BLOCK_TYPE)
                .filter { Log.d("MusicLike", "Found block with blockId: ${it.type} ${it.transaction}");musicLikeBlockValidator.validateTransaction(it.transaction) }
                .shuffled()
                .take(Config.BLOCKS)
        releaseBlocks.forEach {
            musicCommunity.sendBlock(it, randomPeer)
        }
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
