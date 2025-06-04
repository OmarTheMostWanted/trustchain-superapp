package nl.tudelft.trustchain.musicdao.core.ipv8

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.tudelft.ipv8.Peer
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
        val musicProfileBlocks = musicCommunity.database.getBlocksWithType(MusicProfile.BLOCK_TYPE)
            .filter { musicProfileBlockValidator.validateTransaction(it.transaction) }
            .shuffled()
            .take(Config.BLOCKS)

        musicProfileBlocks.forEach {
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
