package nl.tudelft.trustchain.musicdao

import io.mockk.*
import nl.tudelft.ipv8.attestation.trustchain.*
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainStore
import nl.tudelft.trustchain.musicdao.core.ipv8.MusicCommunity
import nl.tudelft.trustchain.musicdao.core.ipv8.SwarmHealth
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MusicCommunityTest {
    private lateinit var database: TrustChainStore
    private lateinit var crawler: TrustChainCrawler
    private lateinit var settings: TrustChainSettings
    private lateinit var community: MusicCommunity

    @Before
    fun setup() {
        database = mockk(relaxed = true)
        crawler = mockk(relaxed = true)
        settings = mockk(relaxed = true)
        community = spyk(MusicCommunity(settings, database, crawler))
        community.myPeer = mockk(relaxed = true)
    }

    @Test
    fun testSendSwarmHealthMessage() {
        every { community.getPeers() } returns emptyList()

        val result = community.sendSwarmHealthMessage(SwarmHealth("infoHash", 1u, 2u))

        assertFalse(result)
    }
}
