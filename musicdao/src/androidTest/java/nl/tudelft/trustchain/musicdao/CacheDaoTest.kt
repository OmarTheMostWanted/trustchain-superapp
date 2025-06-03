package nl.tudelft.trustchain.musicdao

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.MusicLikeEntity
import nl.tudelft.trustchain.musicdao.core.cache.entities.SongEntity
import nl.tudelft.trustchain.musicdao.core.cache.CacheDao
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.ipv8.blocks.Constants.PROTOCOL_VERSION
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheDaoTest {
    private lateinit var db: CacheDatabase
    private lateinit var dao: CacheDao

    private val songs = listOf(
        SongEntity("Test Song 1", "Test Artist", ""),
        SongEntity("Test Song 2", "Test Artist", "")
    )

    @Before
    fun initDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CacheDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dao
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetAlbum() = runBlocking {
        val album = AlbumEntity(
            id = "test-album",
            title = "Test Album",
            artist = "Test Artist",
            infoHash = "",
            publisher = "",
            songs = songs,
            isDownloaded = false,
            magnet = "",
            releaseDate = "",
            cover = "",
            root = "",
            torrentPath = ""
        )

        dao.insert(album)
        val result = dao.get("test-album")

        Assert.assertEquals("Test Album", result.title)
        Assert.assertEquals("Test Artist", result.artist)
        Assert.assertEquals(album.songs.size, result.songs.size)
        Assert.assertEquals(album.songs[0].title, result.songs[0].title)
    }

    @Test
    fun insertAndDeleteAlbum() = runBlocking {
        val album = AlbumEntity(
            id = "test-album-2",
            title = "Test Album 2",
            artist = "Test Artist",
            songs = songs,
            isDownloaded = false,
            infoHash = "",
            publisher = "",
            magnet = "",
            releaseDate = "",
            cover = "",
            root = "",
            torrentPath = ""
        )

        dao.insert(album)
        dao.delete("test-album-2")

        val all = dao.getAll()
        Assert.assertTrue(all.none { it.id == "test-album-2" })
    }

    @Test
    fun insertMusicLikeAndQuery() = runBlocking {
        val like = MusicLikeEntity(
            id = "Test Like",
            name = "Test User",
            likedMusicId = "Test Song 1",
            publicKey = "",
            protocolVersion = PROTOCOL_VERSION
        )

        dao.insertMusicLike(like)

        val likes = dao.getCurrentVersionLikes()
        Assert.assertTrue(likes.any { it.likedMusicId == "Test Song 1" })
    }
}
