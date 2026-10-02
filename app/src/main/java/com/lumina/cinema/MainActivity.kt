package com.lumina.cinema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CinemaScreen()
                }
            }
        }
    }
}

@Composable
fun CinemaScreen() {
    val context = LocalContext.current
    var link by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val player = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(e: PlaybackException) {
                error = "Erro ao reproduzir: ${e.errorCodeName}"
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text("Lumina Cinema", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = link,
            onValueChange = { link = it },
            label = { Text("Cole o link do Drive") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val url = DriveResolver.directUrl(link)
                if (url == null) {
                    error = "Link inválido"
                } else {
                    error = null
                    player.setMediaItem(MediaItem.fromUri(url))
                    player.prepare()
                    player.playWhenReady = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Assistir") }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { this.player = player } },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )
    }
}
