package nl.tudelft.trustchain.musicdao

import android.util.Log
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.tudelft.trustchain.musicdao.core.cache.CacheDao
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicLikeEntity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfile
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.musicLike.MusicProfileBlockRepository
import nl.tudelft.trustchain.musicdao.core.repositories.MusicProfileRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MusicLikeRepositoryTest {
    private lateinit var dao: CacheDao
    private lateinit var db: CacheDatabase
    private lateinit var blockRepository: MusicProfileBlockRepository
    private lateinit var repository: MusicProfileRepository

    private val testSong =
        Song(
            title = "Test Song",
            artist = "Test Artist",
            file = null
        )

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        dao = mockk(relaxed = true)
        db = mockk()
        blockRepository = mockk()
        every { db.dao } returns dao
        every { blockRepository.myPeerPublicKey } returns "peerKey"

        repository = MusicProfileRepository(db, blockRepository)
    }

    @Test
    fun testGetLikes() =
        runTest {
            val likes =
                listOf(
                    MusicLikeEntity("pubKey", "song1", Constants.PROTOCOL_VERSION),
                    MusicLikeEntity("pubKey", "song2", Constants.PROTOCOL_VERSION)
                )

            coEvery { dao.getCurrentVersionLikes() } returns likes

            val result = repository.getLikes()

            assertEquals(2, result.size)
            assertEquals("track1", result[0].songName)
            assertEquals("track2", result[1].songName)
        }

    @Test
    fun testCreateMusicLike() =
        runTest {
            coEvery {
                dao.isSongLikedByUser(any(), any())
            } returns flowOf(true)

            val result = repository.toggleLike(testSong)

            assertNull(result)
            coVerify(exactly = 0) { blockRepository.create(any()) }
        }

    @Test
    fun testInsertMusicLikeBlock() =
        runTest {
            val songs = listOf("song1")
            val tags = emptyMap<String, List<String>>()
            val block =
                MusicProfile(
                    publicKey = "peerKey",
                    songs,
                    protocolVersion = Constants.PROTOCOL_VERSION,
                    tags
                )

            coEvery { dao.isSongLikedByUser(any(), any()) } returns flowOf(false)
            coEvery { blockRepository.create(any()) } returns
                mockk {
                    every { transaction } returns mockk()
                }
            mockkObject(MusicProfile.Companion)
            every { MusicProfile.fromTrustChainTransaction(any()) } returns block
            coEvery { dao.addLikedSong(any()) } just Runs

            val result = repository.toggleLike(testSong)

            assertNotNull(result)
            assertEquals(songs, result?.likedSongs)
        }

    @Test
    fun testRefreshCache() =
        runTest {
            val tags = emptyMap<String, List<String>>()
            val block1 = MusicProfile("pubKey1", listOf("song1"), Constants.PROTOCOL_VERSION, tags)
            val block2 = MusicProfile("pubKey2", listOf("song2"), Constants.PROTOCOL_VERSION, tags)

            coEvery { blockRepository.getAll() } returns listOf(block1, block2)
            coEvery { dao.addLikedSong(any()) } just Runs

            repository.refreshCache()

            coVerify(exactly = 2) { dao.addLikedSong(any()) }
        }

    @Test
    fun testIsSongLikedByUser() {
        val expectedFlow = flowOf(true)
        every {
            dao.isSongLikedByUser("peerKey", "song1")
        } returns expectedFlow

        val result = repository.isSongLikedByMe(testSong)

        assertEquals(expectedFlow, result)
    }
}
