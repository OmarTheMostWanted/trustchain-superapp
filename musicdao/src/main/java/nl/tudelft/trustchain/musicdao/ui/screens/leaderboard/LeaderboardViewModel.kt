package nl.tudelft.trustchain.musicdao.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import androidx.lifecycle.viewModelScope
import nl.tudelft.trustchain.musicdao.core.repositories.MusicProfileRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import nl.tudelft.trustchain.musicdao.core.repositories.model.TagCount
import javax.inject.Inject

@HiltViewModel
<<<<<<< HEAD
class LeaderboardViewModel
    @Inject
    constructor(
        val musicLikeRepository: MusicLikeRepository
    ) : ViewModel()
=======
class LeaderboardViewModel @Inject constructor(
    val musicLikeRepository: MusicProfileRepository
) : ViewModel() {
    val likesFlow: StateFlow<List<MusicLike>> = musicLikeRepository.getLikesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getTopTagsForSong(song: Song): List<TagCount> {
        return musicLikeRepository.getTopTagsForSong(song)
    }
}
>>>>>>> master
