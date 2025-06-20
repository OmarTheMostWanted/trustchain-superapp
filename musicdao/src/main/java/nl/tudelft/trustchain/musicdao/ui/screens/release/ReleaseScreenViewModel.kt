package nl.tudelft.trustchain.musicdao.ui.screens.release

import android.util.Log
import androidx.lifecycle.*
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import nl.tudelft.trustchain.musicdao.core.repositories.model.Album
import nl.tudelft.trustchain.musicdao.core.torrent.TorrentEngine
import nl.tudelft.trustchain.musicdao.core.torrent.status.TorrentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.tudelft.trustchain.musicdao.core.repositories.MusicProfileRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
@HiltViewModel
class ReleaseScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val database: CacheDatabase,
    private val torrentEngine: TorrentEngine,
    private val musicProfileRepository: MusicProfileRepository
) : ViewModel() {

        private val releaseId: String = checkNotNull(savedStateHandle["releaseId"])
        private var releaseLiveData: LiveData<AlbumEntity> = MutableLiveData(null)
        var saturatedReleaseState: LiveData<Album?> = MutableLiveData()

        private val _torrentState: MutableStateFlow<TorrentStatus?> = MutableStateFlow(null)
        val torrentState: StateFlow<TorrentStatus?> = _torrentState


    init {
        viewModelScope.launch {
            releaseLiveData = database.dao.getLiveData(releaseId)
            saturatedReleaseState = releaseLiveData.map { it.toAlbum() }

            val release = database.dao.get(releaseId)
            Log.d("MusicProfile", "Fetched release ${release.title} with magnet ${release.magnet}")

            release.let { _release ->
                if (!_release.isDownloaded) {
                    torrentEngine.download(_release.magnet)
                }

                while (isActive) {
                    if (_release.infoHash != null) {
                        _torrentState.value = torrentEngine.getTorrentStatus(_release.infoHash)
                    }
                }
            }
        }
        }

    suspend fun likeMusic(
        track: Song,
    ) {
        Log.d("MusicProfile", "Liking song: ${track.title}")
        val block = musicProfileRepository.toggleLike(track)
        if (block == null) {
            Log.d("MusicProfile", "Failed to like song: ${track.title}")
        }
    }

    suspend fun unlikeMusic(track: Song) {
        Log.d("MusicProfile", "Unliking song: ${track.title}")
        val block = musicProfileRepository.toggleLike(track)
        if (block == null) {
            Log.d("MusicProfile", "Failed to unlike song: ${track.title}")
        }
    }

    fun isMusicLikedByMe(
        track: Song,
    ): Flow<Boolean> {
        val res = musicProfileRepository.isSongLikedByMe(track)
        Log.d("MusicProfile", "Checking if song ${track.title} is liked by me")
        return res
    }

    suspend fun toggleTag(song: Song, tag: String) {
        musicProfileRepository.toggleTag(song, tag)
    }

    suspend fun getSelectedTags(song: Song): List<String> {
        return musicProfileRepository.getUserTagsForSong(song)
    }


}
