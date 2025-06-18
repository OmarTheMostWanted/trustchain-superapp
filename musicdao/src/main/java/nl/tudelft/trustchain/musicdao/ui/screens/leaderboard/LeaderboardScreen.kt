package nl.tudelft.trustchain.musicdao.ui.screens.leaderboard

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nl.tudelft.trustchain.musicdao.core.repositories.model.Album
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import nl.tudelft.trustchain.musicdao.core.repositories.MusicProfileRepository
import nl.tudelft.trustchain.musicdao.core.repositories.model.MusicLike
import nl.tudelft.trustchain.musicdao.ui.navigation.Screen

@Composable
fun LeaderboardScreen(
    albums: List<Album>,
    navController: NavController,
    leaderboardViewModel: LeaderboardViewModel
) {
    val likes by leaderboardViewModel.likesFlow.collectAsState()

    val likesByMusicId = remember(likes) {
        likes.groupingBy { it.songName }.eachCount()
    }

    val songsWithLikes =
        albums
            .flatMap { it.songs.orEmpty() }
            .distinctBy { song -> song.artist to song.title }
            .map { song ->
                val likeCount = likesByMusicId[MusicLike.musicLikeIdFromSong(song)] ?: 0
                song to likeCount
            }.sortedByDescending { it.second }

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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
        ) {
            item(0) {
                Divider()
            }
            itemsIndexed(songsWithLikes) { index, (song, likes) ->
                var topTags by remember { mutableStateOf<List<String>>(emptyList()) }

                LaunchedEffect(song) {
                    val tags = leaderboardViewModel.getTopTagsForSong(song)
                    topTags = tags.map { it.tag }
                }
                // Find the album this song belongs to
                val album = albums.firstOrNull {
                    it.songs?.any { s -> s.title ==  song.title && s.artist == song.artist } == true
                }
                val albumId = album?.id ?: return@itemsIndexed

                LeaderboardListItem(
                    song = song,
                    likes = likes,
                    rank = index + 1,
                    topTags = topTags,
                    onClick = {
                        // Stores the clicked song’s info so the next screen can access it.
                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                            set("songTitle", song.title)
                            set("songArtist", song.artist)
                        }
                        // Navigate to the album
                        navController.navigate(Screen.Release.createRoute(albumId))
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
    topTags: List<String>,
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
            Column {
                Text(
                    song.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    topTags.forEach { tag ->
                        Text(
                            text = tag,
                            fontSize = 12.sp,
                            color = Color.Green,
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF4CAF50),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        trailing = {
            Text(
                "$likes likes",
                style = MaterialTheme.typography.body1
            )
        },
        // Handle click to trigger navigation
        modifier = Modifier.clickable { onClick() }
    )
}
