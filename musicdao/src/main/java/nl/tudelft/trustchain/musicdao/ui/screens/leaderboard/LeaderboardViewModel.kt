package nl.tudelft.trustchain.musicdao.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import nl.tudelft.trustchain.musicdao.core.repositories.MusicLikeRepository
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    val musicLikeRepository: MusicLikeRepository
) : ViewModel()