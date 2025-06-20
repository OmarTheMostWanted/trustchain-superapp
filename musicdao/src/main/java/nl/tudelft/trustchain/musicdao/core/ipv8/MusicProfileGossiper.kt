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

                    gossip()
                    delay(Config.DELAY)
                }
            }
        }

        fun gossip() {
            val randomPeer = pickRandomPeer()
            if (randomPeer == null) {
                Log.d("MusicProfile", "No peers available for gossiping music profile")
                return
            }

            val validTrustChainBlocks =
                musicCommunity.database
                    .getBlocksWithType(MusicProfile.BLOCK_TYPE)
                    .filter { musicProfileBlockValidator.validateTransaction(it.transaction) }



            val groupedByPublicKey = validTrustChainBlocks.groupBy { it.publicKey.toHex() }

            val newestBlocks =
                groupedByPublicKey.map { entry ->
                    entry.value.maxByOrNull { it.timestamp } ?: entry.value.first()
                }

            newestBlocks.shuffled().take(Config.BLOCKS).forEach { block ->
                musicCommunity.sendBlock(block, randomPeer)
            }


        }

        object Config {
            const val BLOCKS = 10
            const val DELAY = 5_000L
        }

        fun pickRandomPeer(): Peer? {
            val peers = musicCommunity.getPeers()
            if (peers.isEmpty()) return null
            return peers.random()
        }
    }
