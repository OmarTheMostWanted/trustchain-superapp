package nl.tudelft.trustchain.musicdao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheDaoTest {
  private var db: AppDatabase
  private var cacheDao: CacheDao

  @Before
  fun setup() {
    database = Room.inMemoryDatabaseBuilder<Dao>(
      ApplicationProvider.getApplicationContext(),
      AppDatabase::class.java
    ).allowMainThreadQueries().build()
    cacheDao = db.cacheDao()
  }

  @After
  fun teardown() {
    db.close()
  }

  @Test
  fun testInsertAndRetrieveAlbumEntity() = runBlocking {
    val album = AlbumEntity(id = "1", title = "Test Album", artist = "Test Artist", infoHash = "hash", publisher = "publisher")
    cacheDao.insert(album)

    val retrievedAlbum = cacheDao.get("1")
    Assert.assertEquals(album, retrievedAlbum)
  }

  @Test
  fun testDeleteAlbumEntity() = runBlocking {
    val album = AlbumEntity(id = "2", title = "Delete Album", artist = "Artist", infoHash = "hash", publisher = "publisher")
    cacheDao.insert(album)
    cacheDao.delete("2")

    val retrievedAlbum = cacheDao.get("2")
    Assert.assertNull(retrievedAlbum)
  }

  @Test
  fun testUpdateAlbumEntity() = runBlocking {
    val album = AlbumEntity(id = "3", title = "Old Title", artist = "Artist", infoHash = "hash", publisher = "publisher")
    cacheDao.insert(album)

    val updatedAlbum = album.copy(title = "New Title")
    cacheDao.update(updatedAlbum)

    val retrievedAlbum = cacheDao.get("3")
    Assert.assertEquals("New Title", retrievedAlbum.title)
  }

  @Test
  fun testGetAllAlbums() = runBlocking {
      val album1 = AlbumEntity(
          id = "4",
          title = "Album 1",
          artist = "Artist 1",
          infoHash = "hash1",
          publisher = "publisher1"
      )
      val album2 = AlbumEntity(
          id = "5",
          title = "Album 2",
          artist = "Artist 2",
          infoHash = "hash2",
          publisher = "publisher2"
      )
      cacheDao.insert(album1)
      cacheDao.insert(album2)

      val allAlbums = cacheDao.getAll()
      Assert.assertEquals(2, allAlbums.size)
  }
}
