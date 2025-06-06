package nl.tudelft.trustchain.musicdao

import android.util.Log
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.tudelft.trustchain.musicdao.core.cache.CacheDao
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicLikeEntity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlock
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicLikeBlockRepository
import nl.tudelft.trustchain.musicdao.core.repositories.MusicLikeRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MusicLikeRepositoryTest {
    private lateinit var dao: CacheDao
    private lateinit var db: CacheDatabase
    private lateinit var blockRepository: MusicLikeBlockRepository
    private lateinit var repository: MusicLikeRepository

    private val testSong =
        Song(
            title = "Test Song",
            artist = "Test Artist",
            file = null
        )
    private val likedMusicId = MusicLike.musicLikeIdFromSong(testSong)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        dao = mockk(relaxed = true)
        db = mockk()
        blockRepository = mockk()
        every { db.dao } returns dao
        every { blockRepository.myPeerPublicKey } returns "peerKey"

        repository = MusicLikeRepository(db, blockRepository)
    }

    @Test
    fun testGetLikes() =
        runTest {
            val entities =
                listOf(
                    MusicLikeEntity("id1", "pubKey", "user", "track1", Constants.PROTOCOL_VERSION),
                    MusicLikeEntity("id2", "pubKey", "user", "track2", Constants.PROTOCOL_VERSION)
                )

            coEvery { dao.getCurrentVersionLikes() } returns entities

            val result = repository.getLikes()

            assertEquals(2, result.size)
            assertEquals("track1", result[0].likedMusicId)
            assertEquals("track2", result[1].likedMusicId)
        }

    @Test
    fun testCreateMusicLike() =
        runTest {
            coEvery {
                dao.isSongLikedByMe(any(), any())
            } returns flowOf(true)

            val result = repository.createMusicLike(testSong)

            assertNull(result)
            coVerify(exactly = 0) { blockRepository.create(any()) }
        }

    @Test
    fun testInsertMusicLikeBlock() =
        runTest {
            val block =
                MusicLikeBlock(
                    likedMusicId = likedMusicId,
                    name = "user",
                    publicKey = "peerKey",
                    protocolVersion = Constants.PROTOCOL_VERSION
                )

            coEvery { dao.isSongLikedByMe(any(), any()) } returns flowOf(false)
            coEvery { blockRepository.create(any()) } returns
                mockk {
                    every { transaction } returns mockk()
                }
            mockkObject(MusicLikeBlock.Companion)
            every { MusicLikeBlock.fromTrustChainTransaction(any()) } returns block
            coEvery { dao.insertMusicLike(any()) } just Runs

            val result = repository.createMusicLike(testSong)

            assertNotNull(result)
            assertEquals(likedMusicId, result?.likedMusicId)
            coVerify { dao.insertMusicLike(match { it.likedMusicId == likedMusicId }) }
        }

    @Test
    fun testRefreshCache() =
        runTest {
            val block1 = MusicLikeBlock("track1", "user1", "pub1", Constants.PROTOCOL_VERSION)
            val block2 = MusicLikeBlock("track2", "user2", "pub2", Constants.PROTOCOL_VERSION)

            coEvery { blockRepository.getAllKnownSongLikes() } returns listOf(block1, block2)
            coEvery { dao.insertMusicLike(any()) } just Runs

            repository.refreshCache()

            coVerify(exactly = 2) { dao.insertMusicLike(any()) }
        }

    @Test
    fun testIsSongLikedByMe() {
        val expectedFlow = flowOf(true)
        every {
            dao.isSongLikedByMe(likedMusicId, "peerKey")
        } returns expectedFlow

        val result = repository.isSongLikedByMe(testSong)

        assertEquals(expectedFlow, result)
    }
}
