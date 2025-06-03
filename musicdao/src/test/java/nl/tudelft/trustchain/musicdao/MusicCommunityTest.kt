package nl.tudelft.trustchain.musicdao

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.attestation.trustchain.TrustChainSettings
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainSQLiteStore
import nl.tudelft.ipv8.messaging.EndpointAggregator
import nl.tudelft.ipv8.peerdiscovery.Network
import nl.tudelft.ipv8.peerdiscovery.strategy.RandomWalk
import nl.tudelft.ipv8.sqldelight.Database
import nl.tudelft.trustchain.musicdao.core.ipv8.MusicCommunity
import org.junit.Assert.assertEquals
import org.junit.Test

class MusicCommunityTest {
    private val peersSize = 5
    private val myPeer = mockk<Peer>()
    private val endpoint = mockk<EndpointAggregator>()
    private val network = mockk<Network>(relaxed = true)

    val settings = TrustChainSettings()
    val driver = AndroidSqliteDriver(Database.Schema, this, "music-private.db")
    val store = TrustChainSQLiteStore(Database(driver))
    val randomWalk = RandomWalk.Factory()
    private var musicCommunity = spyk(MusicCommunity(settings, store), recordPrivateCalls = true)

    @Test
    fun musicCommunity_hasPeers() {
        every { musicCommunity.getPeers() } returns getFakePeers()
        every { musicCommunity.myPeer } returns myPeer
        every { musicCommunity.endpoint } returns endpoint
        every { musicCommunity.network } returns network
        every { endpoint.send(any<Peer>(), any()) } just runs
    }

    private fun getFakePeers(): List<Peer> {
        val peers = mutableListOf<Peer>()
        for (i in 1..peersSize) {
            peers.add(mockk<Peer>(relaxed = true))
        }
        return peers
    }
}
