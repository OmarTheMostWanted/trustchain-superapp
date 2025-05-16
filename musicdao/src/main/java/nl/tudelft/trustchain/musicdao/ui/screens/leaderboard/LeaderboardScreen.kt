package nl.tudelft.trustchain.musicdao.ui.screens.leaderboard

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

@Composable
fun LeaderboardScreen(
    albums: List<Album>,
    navController: NavController
) {
    val songsWithLikes = albums
        .flatMap { it.songs.orEmpty() }
        .map { song ->
            song to (1..9).random()
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
                LeaderboardListItem(
                    song = song,
                    likes = likes,
                    rank = index + 1
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
    rank: Int
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
        }
    )
}
