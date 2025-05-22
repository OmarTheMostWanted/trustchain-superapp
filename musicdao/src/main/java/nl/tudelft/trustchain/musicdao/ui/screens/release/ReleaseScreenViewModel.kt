package nl.tudelft.trustchain.musicdao.ui.screens.release

import android.util.Log
import androidx.lifecycle.*
import nl.tudelft.trustchain.musicdao.core.cache.CacheDatabase
import nl.tudelft.trustchain.musicdao.core.cache.entities.AlbumEntity
import nl.tudelft.trustchain.musicdao.core.repositories.model.Album
import nl.tudelft.trustchain.musicdao.core.torrent.TorrentEngine
import nl.tudelft.trustchain.musicdao.core.torrent.status.TorrentStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.tudelft.trustchain.musicdao.core.repositories.MusicLikeRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
@HiltViewModel
class ReleaseScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val database: CacheDatabase,
    private val torrentEngine: TorrentEngine,
    private val musicLikeRepository: MusicLikeRepository
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

            release.let { _release ->
                if (!_release.isDownloaded) {
                    torrentEngine.download(_release.magnet)
                }

                while (isActive) {
                    if (_release.infoHash != null) {
                        _torrentState.value = torrentEngine.getTorrentStatus(_release.infoHash)
                    }
                    delay(1000L)
                }
            }
        }
    }

    suspend fun likeMusic(
        track: Song,
    ) {
        val block = musicLikeRepository.createMusicLike(track)
        if (block != null) {
            Log.d("MusicLike", "${block.name} liked ${block.likedMusicId}")
        }

    }

    fun isMusicLikedByMe(
        track: Song,
    ): Flow<Boolean> {
        return musicLikeRepository.isSongLikedByMe(track)
    }

}
