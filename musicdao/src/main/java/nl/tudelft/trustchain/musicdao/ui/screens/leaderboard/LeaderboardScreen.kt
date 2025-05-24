package nl.tudelft.trustchain.musicdao.ui.screens.leaderboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nl.tudelft.trustchain.musicdao.core.repositories.model.Album
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import androidx.compose.ui.text.style.TextOverflow
import androidx.constraintlayout.motion.widget.MotionScene.Transition.TransitionOnClick
import nl.tudelft.trustchain.musicdao.core.repositories.MusicLikeRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike
import nl.tudelft.trustchain.musicdao.ui.navigation.Screen

@Composable
fun LeaderboardScreen(
    albums: List<Album>,
    navController: NavController,
    musicLikeRepository: MusicLikeRepository
) {
    var likesByMusicId by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        // Fetch all likes from the repository
        val likes = musicLikeRepository.getLikes()
        // Group and count likes by likedMusicId (track.title in current logic)
        likesByMusicId = likes.groupingBy { it.likedMusicId }.eachCount()
    }

    val songsWithLikes = albums
        .flatMap { it.songs.orEmpty() }
        .distinctBy { song -> song.artist to song.title }
        .map { song ->
            val likeCount = likesByMusicId[MusicLike.musicLikeIdFromSong(song)] ?: 0
            song to likeCount
        }
        .sortedByDescending { it.second }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Leaderboard")
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item(0) {
                Divider()
            }
            itemsIndexed(songsWithLikes) { index, (song, likes) ->
                // Find the album this song belongs to
                val album = albums.firstOrNull {
                    it.songs?.any { s -> s.title ==  song.title && s.artist == song.artist } == true
                }
                val albumId = album?.id ?: return@itemsIndexed

                LeaderboardListItem(
                    song = song,
                    likes = likes,
                    rank = index + 1,
                    onClick = {
                        navController.navigate(Screen.Release.createRoute(albumId)) // Handler to navigate to the album screen
                    }
                )
                Divider()
            }
            if (songsWithLikes.isNotEmpty()) {
                item("end") {
                    Column(
                        modifier = Modifier.height(100.dp)
                    ) {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LeaderboardListItem(
    song: Song,
    likes: Int,
    rank: Int,
    onClick: () -> Unit
) {
    ListItem(
        text = {
            Text(
                "$rank. ${song.title}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryText = {
            Text(
                song.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailing = {
            Text(
                "$likes likes",
                style = MaterialTheme.typography.body1
            )
        },
        modifier = Modifier.clickable { onClick() } // Handle click to trigger navigation
    )
}
