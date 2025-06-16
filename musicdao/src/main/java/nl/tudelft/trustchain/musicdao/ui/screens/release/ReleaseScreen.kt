package nl.tudelft.trustchain.musicdao.ui.screens.release

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import nl.tudelft.trustchain.musicdao.MusicActivity
import nl.tudelft.trustchain.musicdao.core.repositories.model.Album
import nl.tudelft.trustchain.musicdao.core.repositories.model.Song
import nl.tudelft.trustchain.musicdao.core.torrent.status.DownloadingTrack
import nl.tudelft.trustchain.musicdao.ui.components.ReleaseCover
import nl.tudelft.trustchain.musicdao.ui.components.player.PlayerViewModel
import nl.tudelft.trustchain.musicdao.ui.util.dateToShortString
import nl.tudelft.trustchain.musicdao.ui.navigation.Screen
import nl.tudelft.trustchain.musicdao.ui.screens.torrent.TorrentStatusScreen
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.tudelft.trustchain.musicdao.ui.SnackbarHandler
import java.io.File

@ExperimentalMaterialApi
@Composable
fun ReleaseScreen(
    playerViewModel: PlayerViewModel,
    navController: NavController
) {
    var state by remember { mutableStateOf(0) }
    val titles = listOf("RELEASE", "TORRENT")
    val coroutineScope = rememberCoroutineScope()

    val viewModel: ReleaseScreenViewModel = hiltViewModel()

    val torrentStatus by viewModel.torrentState.collectAsState()
    val albumState by viewModel.saturatedReleaseState.observeAsState()

    val playingTrack = playerViewModel.playingTrack.collectAsState()

    // Audio Player
    val context = LocalContext.current

    fun play(
        track: Song,
        cover: File?
    ) {
        playerViewModel.playDownloadedTrack(track, cover)
    }

    fun play(
        track: DownloadingTrack,
        cover: File?
    ) {
        playerViewModel.playDownloadingTrack(
            Song(
                file = track.file,
                artist = track.artist,
                title = track.title
            ),
            context,
            cover
        )
    }

    val scrollState = rememberScrollState()

    albumState?.let { album ->

        // Get the values that has been stored from the previous screen
        val songTitle = navController.previousBackStackEntry?.savedStateHandle?.get<String>("songTitle")
        val songArtist = navController.previousBackStackEntry?.savedStateHandle?.get<String>("songArtist")

        LaunchedEffect(songTitle, songArtist) {
            if (songTitle != null && songArtist != null) {
                val songToPlay = album.songs?.firstOrNull {
                    it.title == songTitle && it.artist == songArtist
                }
                if (songToPlay != null) {
                    playerViewModel.playDownloadedTrack(songToPlay, album.cover) // Starts the playback
                }

                // Clean up state so it doesn't auto-trigger again
                navController.previousBackStackEntry?.savedStateHandle?.remove<String>("songTitle")
                navController.previousBackStackEntry?.savedStateHandle?.remove<String>("songArtist")
            }
        }

        LaunchedEffect(
            key1 = playerViewModel,
            block = {
                viewModel.torrentState.collect {
                    val current = playerViewModel.playingTrack.value ?: return@collect
                    val downloadingTracks =
                        viewModel.torrentState.value?.downloadingTracks ?: return@collect
                    val isPlaying = playerViewModel.exoPlayer.isPlaying
                    val targetTrack =
                        downloadingTracks.find { it.file.name == current.file?.name }
                            ?: return@collect

                    if (!isPlaying && targetTrack.progress > 20 && targetTrack.progress < 99) {
                        play(targetTrack, album.cover)
                    }
                }
            }
        )

        Column(
            modifier =
                Modifier
                    .verticalScroll(scrollState)
                    .padding(bottom = 150.dp)
        ) {
            TabRow(selectedTabIndex = state) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        onClick = { state = index },
                        selected = (index == state),
                        text = { Text(title) }
                    )
                }
            }
            if (state == 0) {
                Column(
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 20.dp)
                ) {
                    ReleaseCover(
                        file = album.cover,
                        modifier =
                            Modifier
                                .height(200.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10))
                                .background(Color.DarkGray)
                                .shadow(10.dp)
                                .align(Alignment.CenterHorizontally)
                    )
                }
                Header(album, navController = navController)
                if (!album.songs.isNullOrEmpty()) {
                    val files = album.songs
                    files.map {
                        val selectedTags = remember { mutableStateListOf<String>() }
                        var expanded by remember { mutableStateOf(false) }
                        val genreOptions = listOf("Rock", "Jazz", "Pop", "Hip-Hop", "Electronic", "Classical")

                        LaunchedEffect(it) {
                            val newTags = viewModel.getSelectedTags(it)
                            if (newTags.toSet() != selectedTags.toSet()) {
                                selectedTags.clear()
                                selectedTags.addAll(newTags)
                            }
                        }

                        val isPlayingModifier =
                            playingTrack.value?.let { current ->
                                if (it.title == current.title) {
                                    MaterialTheme.colors.primary
                                } else {
                                    MaterialTheme.colors.onBackground
                                }
                            } ?: MaterialTheme.colors.onBackground

                        ListItem(
                            text = { Text(it.title, color = isPlayingModifier, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            secondaryText = { Text(it.artist, color = isPlayingModifier) },
                            trailing = {
                                val isLiked by viewModel.isMusicLikedByMe(it).collectAsState(initial = false)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                if (isLiked) {
                                                    viewModel.unlikeMusic(it) // Call unlike function
                                                    Toast.makeText(context, "Unliked song from album ${album.title}", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    viewModel.likeMusic(it) // Call like function
                                                    Toast.makeText(context, "Liked song from album ${album.title}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Favorite,
                                            contentDescription = if (isLiked) "Unlike" else "Like",
                                            tint = if (isLiked) MaterialTheme.colors.primary else Color.Gray // Green when liked, Gray when unliked
                                        )
                                    }
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(imageVector = Icons.Default.List, contentDescription = "Select Tags")
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        genreOptions.forEach { tag ->
                                            val selected = tag in selectedTags
                                            DropdownMenuItem(onClick = {
                                                coroutineScope.launch {
                                                    viewModel.toggleTag(it, tag)
                                                    selectedTags.clear()
                                                    selectedTags.addAll(viewModel.getSelectedTags(it))
                                                }
                                            }) {
                                                Text(tag, color = if (selected) Color.Green else Color.Unspecified)
                                            }
                                        }
                                    }

                                }
                            },
                            modifier = Modifier.clickable { play(it, album.cover) }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, top = .5.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            selectedTags.forEach { tag ->
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    color = Color.Green, // Green
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
                } else {
                    if (torrentStatus != null) {
                        val downloadingTracks = torrentStatus?.downloadingTracks
                        downloadingTracks?.map {
                            ListItem(
                                text = { Text(it.title) },
                                secondaryText = {
                                    Column {
                                        Text(album.artist, modifier = Modifier.padding(bottom = 5.dp))
                                        LinearProgressIndicator(progress = it.progress.toFloat() / 100)
                                    }
                                },
                                trailing = {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = null
                                    )
                                },
                                modifier =
                                    Modifier.clickable {
                                        play(it, album.cover)
                                    }
                            )
                        }
                        if (downloadingTracks.isNullOrEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
            if (state == 1) {
                val current = torrentStatus
                if (current != null) {
                    TorrentStatusScreen(current)
                } else {
                    Text("Could not find torrent.")
                }
            }
        }
    }
}

@Composable
fun Header(
    album: Album,
    navController: NavController
) {
    Column(modifier = Modifier.padding(top = 10.dp, start = 20.dp, end = 20.dp)) {
        Text(
            album.title,
            style = MaterialTheme.typography.h6.merge(SpanStyle(fontWeight = FontWeight.ExtraBold)),
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            album.artist,
            style = MaterialTheme.typography.body2.merge(SpanStyle(fontWeight = FontWeight.SemiBold)),
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            "UUID",
            style = MaterialTheme.typography.body2.merge(SpanStyle(fontWeight = FontWeight.SemiBold)),
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            album.id,
            style = MaterialTheme.typography.body2.merge(SpanStyle(fontWeight = FontWeight.SemiBold)),
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            "Artist Public Key",
            style = MaterialTheme.typography.body2.merge(SpanStyle(fontWeight = FontWeight.SemiBold)),
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            album.publisher,
            style = MaterialTheme.typography.body2.merge(SpanStyle(fontWeight = FontWeight.SemiBold)),
            modifier = Modifier.padding(bottom = 5.dp)
        )

        Text(
            "Album - ${dateToShortString(album.releaseDate.toString())}",
            style =
                MaterialTheme.typography.body2.merge(
                    SpanStyle(fontWeight = FontWeight.SemiBold, color = Color.Gray)
                ),
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                IconButton(
                    onClick = {
                        navController.navigate(
                            Screen.Profile.createRoute(publicKey = album.publisher)
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null
                    )
                }
                IconButton(
                    onClick = {
                        navController.navigate(
                            Screen.Donate.createRoute(publicKey = album.publisher)
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null
                    )
                }

                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.TopStart)) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                navController.navigate(
                                    Screen.Profile.createRoute(publicKey = album.publisher)
                                )
                            }
                        ) {
                            Text("View Artist")
                        }
                        DropdownMenuItem(
                            onClick = {
                                navController.navigate(
                                    Screen.Donate.createRoute(publicKey = album.publisher)
                                )
                            }
                        ) {
                            Text("Donate")
                        }
                        DropdownMenuItem(onClick = { }) {
                            Text("View Meta-data")
                        }
                    }
                }
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = null
                )
            }
        }
    }
}
