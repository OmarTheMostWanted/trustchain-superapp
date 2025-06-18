package nl.tudelft.trustchain.musicdao

import io.mockk.*
import kotlinx.coroutines.test.runTest
import nl.tudelft.trustchain.musicdao.core.cache.CacheDao
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicLikeEntity
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class CacheDaoTest {
    private lateinit var dao: CacheDao

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
    }

    @Test
    fun testGetAlbum() =
        runTest {
            val album =
                AlbumEntity(
                    id = "album1",
                    title = "Mock Album",
                    artist = "Mock Artist",
                    songs = listOf(),
                    isDownloaded = false,
                    infoHash = "",
                    publisher = "",
                    magnet = "",
                    releaseDate = "",
                    cover = "",
                    root = "",
                    torrentPath = ""
                )

            coEvery { dao.get("album1") } returns album

            val result = dao.get("album1")

            assertEquals("Mock Album", result.title)
            assertEquals("Mock Artist", result.artist)
            coVerify { dao.get("album1") }
        }

    @Test
    fun testInsertMusicLike() =
        runTest {
            val like =
                MusicLikeEntity(
                    publicKey = "pubKey",
                    protocolVersion = "",
                    songName = "song1"
                )

            coEvery { dao.insertMusicLike(like) } just Runs

            dao.insertMusicLike(like)

            coVerify { dao.insertMusicLike(like) }
        }

    @Test
    fun testGetCurrentVersionLikes() =
        runTest {
            val likes =
                listOf(
                    MusicLikeEntity("like1", "pubKey", "Test User", "song1", Constants.PROTOCOL_VERSION),
                    MusicLikeEntity("like2", "pubKey", "Test User", "song2", Constants.PROTOCOL_VERSION)
                )

            coEvery { dao.getCurrentVersionLikes(Constants.PROTOCOL_VERSION) } returns likes

            val result = dao.getCurrentVersionLikes(Constants.PROTOCOL_VERSION)

            assertEquals(2, result.size)
            assertTrue(result.any { it.likedMusicId == "song1" })
            coVerify { dao.getCurrentVersionLikes(Constants.PROTOCOL_VERSION) }
        }
}
