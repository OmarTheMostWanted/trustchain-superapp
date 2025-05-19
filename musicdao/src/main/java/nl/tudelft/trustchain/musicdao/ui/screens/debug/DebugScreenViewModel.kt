package nl.tudelft.trustchain.musicdao.ui.screens.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import nl.tudelft.trustchain.musicdao.core.torrent.TorrentEngine
import nl.tudelft.trustchain.musicdao.core.torrent.status.SessionManagerStatus
import nl.tudelft.trustchain.musicdao.core.torrent.status.TorrentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.tudelft.trustchain.musicdao.core.repositories.MusicLikeRepository
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike

@OptIn(DelicateCoroutinesApi::class)
@HiltViewModel
class DebugScreenViewModel
    @Inject
    constructor(
        private val torrentEngine: TorrentEngine,
        private val musicLikeRepository: MusicLikeRepository
    ) : ViewModel() {
        private val _status: MutableStateFlow<List<TorrentStatus>> = MutableStateFlow(listOf())
        val status: StateFlow<List<TorrentStatus>> = _status

        private val _sessionStatus: MutableStateFlow<SessionManagerStatus?> = MutableStateFlow(null)
        val sessionStatus: StateFlow<SessionManagerStatus?> = _sessionStatus

        init {
            viewModelScope.launch {
                while (isActive) {
                    _status.value = torrentEngine.getAllTorrentStatus()
                    delay(5000L)
                }
            }

            viewModelScope.launch {
                while (isActive) {
                    _sessionStatus.value = torrentEngine.getSessionManagerStatus()
                    delay(2000)
                }
            }
        }

        fun getAllLikes() {
            var likes: List<MusicLike> = listOf();
            viewModelScope.launch(Dispatchers.IO) {
                likes = musicLikeRepository.getLikes()
                Log.d("MusicLike", "Getting likes")
                for (like in likes) {
                    Log.d("MusicLike", "${like.likedMusicId} liked by ${like.name}")
                }
            }
            Log.d("MusicLike", "Got likes")
        }
    }
