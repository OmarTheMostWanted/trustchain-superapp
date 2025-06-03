package nl.tudelft.trustchain.musicdao

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.ipv8.MusicCommunity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlockRepository
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlockValidator
import nl.tudelft.trustchain.musicdao.core.repositories.MusicLikeRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class MusicLikeRepositoryTest {
    private lateinit var db: CacheDatabase
    private lateinit var musicLikeBlockRepository: MusicLikeBlockRepository
    private lateinit var repository: MusicLikeRepository
    private lateinit var musicCommunity: MusicCommunity
    private lateinit var validator: MusicLikeBlockValidator

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CacheDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        musicCommunity = mock()
        validator = mock()

        val publicKeyBytes = "peer_key".toByteArray()
        val publicKeyMock = mock<PublicKey> {
            on { keyToBin() } doReturn publicKeyBytes
        }

        val peerMock = mock<Peer> {
            on { publicKey } doReturn publicKeyMock
        }

        whenever(musicCommunity.myPeer).thenReturn(peerMock)
        whenever(musicCommunity.publicKeyStringToByteArray(any())).thenReturn(publicKeyBytes)
        whenever(musicCommunity.createProposalBlock(any(), any(), any())).thenAnswer {
            mock<TrustChainBlock>().apply {
                whenever(transaction).thenReturn(
                    mapOf(
                        "publicKey" to "peer_key",
                        "name" to "peer_key",
                        "likedMusicId" to "test-song-1",
                        "protocolVersion" to "1.0"
                    )
                )
            }
        }

        whenever(validator.validateTransaction(any())).thenReturn(true)

        musicLikeBlockRepository = MusicLikeBlockRepository(musicCommunity, validator)
        repository = MusicLikeRepository(db, musicLikeBlockRepository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createLikeAndGet() = runBlocking {
        val song = Song(
            id = "test-song",
            albumId = "test-album",
            title = "Test Song",
            artist = "Test Artist",
            duration = 120,
        )

        val result = repository.createMusicLike(song)
        Assert.assertNotNull(result)

        val allLikes = db.dao.getCurrentVersionLikes()
        Assert.assertEquals(1, allLikes.size)
        Assert.assertEquals("test-song", allLikes.first().likedMusicId)
        Assert.assertEquals("peer_key", allLikes.first().publicKey)
    }
}

