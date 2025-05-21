package nl.tudelft.trustchain.musicdao.ui.screens.vote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun VoteScreen() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
    ) {
        Card {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Spacer(modifier = Modifier.size(20.dp))
                    Column {
                        Text(
                            "Votes",
                            style = MaterialTheme.typography.h6,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Text("5 votes left", style = MaterialTheme.typography.body1)
                    }
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(onClick = {}) {
                        Text("Join")
                    }
                }
            }
        }
    }
}
