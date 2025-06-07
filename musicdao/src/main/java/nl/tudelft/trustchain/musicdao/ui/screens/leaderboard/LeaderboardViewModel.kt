package nl.tudelft.trustchain.musicdao.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import nl.tudelft.trustchain.musicdao.core.repositories.MusicProfileRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import nl.tudelft.trustchain.musicdao.core.repositories.model.TagCount
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    val musicLikeRepository: MusicProfileRepository
) : ViewModel() {
    suspend fun getTopTagsForSong(song: Song): List<TagCount> {
        return musicLikeRepository.getTopTagsForSong(song)
    }
}
