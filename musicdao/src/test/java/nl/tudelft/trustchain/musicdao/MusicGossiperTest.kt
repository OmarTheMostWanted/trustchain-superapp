package nl.tudelft.trustchain.musicdao

import io.mockk.*
import kotlinx.coroutines.test.runTest
import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.trustchain.musicdao.core.ipv8.MusicCommunity
import nl.tudelft.trustchain.musicdao.core.ipv8.MusicProfileGossiper
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfile
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfileBlockValidator
import org.junit.Before
import org.junit.Test

class MusicGossiperTest {
    private lateinit var musicCommunity: MusicCommunity
    private lateinit var blockValidator: MusicProfileBlockValidator
    private lateinit var gossiper: MusicProfileGossiper

    private val mockPeer = mockk<Peer>(relaxed = true)
    private val mockBlock = mockk<TrustChainBlock>(relaxed = true)

    @Before
    fun setup() {
        musicCommunity = mockk(relaxed = true)
        blockValidator = mockk()
        gossiper = spyk(MusicProfileGossiper(musicCommunity, blockValidator))
    }

    @Test
    fun testGossipRandomPeer() =
        runTest {
            every { musicCommunity.getPeers() } returns listOf(mockPeer)
            every { musicCommunity.database.getBlocksWithType(MusicProfile.BLOCK_TYPE) } returns listOf(mockBlock, mockBlock)
            every { blockValidator.validateTransaction(any()) } returns true
            every { mockBlock.transaction } returns mapOf("likedMusicId" to "some_id")
            every { gossiper.pickRandomPeer() } returns mockPeer

            gossiper.gossip()
            verify(atLeast = 1) { musicCommunity.sendBlock(mockBlock, mockPeer) }
        }

    @Test
    fun testGossipNoPeers() =
        runTest {
            every { musicCommunity.getPeers() } returns emptyList()
            every { musicCommunity.database.getBlocksWithType(MusicProfile.BLOCK_TYPE) } returns listOf(mockBlock)
            every { blockValidator.validateTransaction(any()) } returns true

            gossiper.gossip()
            verify(exactly = 1) { musicCommunity.sendBlock(any(), null) }
        }

    @Test
    fun testGossipSkipsInvalidTransactions() =
        runTest {
            every { musicCommunity.getPeers() } returns listOf(mockPeer)
            every { musicCommunity.database.getBlocksWithType(MusicProfile.BLOCK_TYPE) } returns listOf(mockBlock)
            every { blockValidator.validateTransaction(any()) } returns false
            every { gossiper.pickRandomPeer() } returns mockPeer

            gossiper.gossip()
            verify(exactly = 0) { musicCommunity.sendBlock(any(), any()) }
        }
}
