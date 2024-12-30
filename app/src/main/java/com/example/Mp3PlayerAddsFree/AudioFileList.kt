package com.example.Mp3PlayerAddsFree

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DrawerDefaults.backgroundColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.rememberAsyncImagePainter


/**  val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }
    AndroidView(factory = {PlayerView(context).apply{ player = exoPlayer }},
        modifier = Modifier.fillMaxWidth())
} **/





@Composable
fun AudioFileList(contentResolver: ContentResolver) {

    val expandedStates = remember { mutableStateOf(mutableMapOf<Uri, Boolean>()) }
    var audioFiles by remember { mutableStateOf(listOf<AudioFile>()) }
    var currentlyPlayingAudio by remember { mutableStateOf<AudioFile?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isFullScreen by remember { mutableStateOf(false) } // Fullscreen toggle state

    val filterAudioFiles = audioFiles.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    fun playAudio(uri:Uri, exoPlayer: ExoPlayer){
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }



    fun formatTime(ms: Long): String {
        val minutes = ms / 1000 / 60
        val seconds = (ms / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build()
    }


    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        audioFiles = loadAudioFiles(contentResolver)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (!isFullScreen) {
                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    singleLine = true
                )

                // Song List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // Take up remaining space
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    items(filterAudioFiles) { audioFile ->
                        val isExpanded = expandedStates.value[audioFile.uri] ?: false

                        Row( // Use Row to align album art and song details horizontally
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .padding(10.dp)
                                .clickable { currentlyPlayingAudio = audioFile

                                    playAudio(audioFile.uri, exoPlayer)

                                }
                        ) {
                            // Album Art
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = audioFile.albumArt ?: R.drawable.emptysong,
                                    placeholder = painterResource(id = R.drawable.emptysong),
                                    error = painterResource(id = R.drawable.emptysong)
                                ),
                                contentDescription = "Album Art",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .size(50.dp) // Size of album art
                                    .clip(RoundedCornerShape(4.dp))
                            )

                            Spacer(modifier = Modifier.width(8.dp)) // Space between album art and text

                            Column {
                                Text(
                                    color = MaterialTheme.colorScheme.primary,
                                    text = audioFile.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Duration: ${formatTime(audioFile.duration)}",
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Artist: ${audioFile.artist ?: "Unknown"}",
                                    color = MaterialTheme.colorScheme.inversePrimary,
                                    style = MaterialTheme.typography.bodySmall
                                )

                            }

                        }
                    }
                }
            } else {
                FullScreenPlayer(
                    audioFile = currentlyPlayingAudio,
                    exoPlayer = exoPlayer,
                    onNext = {
                        val currentIndex = audioFiles.indexOf(currentlyPlayingAudio)
                        if (currentIndex != -1) {
                            val nextIndex = (currentIndex + 1) % audioFiles.size
                            currentlyPlayingAudio = audioFiles[nextIndex]
                            playAudio(audioFiles[nextIndex].uri, exoPlayer)
                        }
                    },
                    onPrevious = {
                        val currentIndex = audioFiles.indexOf(currentlyPlayingAudio)
                        if (currentIndex != -1) {
                            val previousIndex =
                                if (currentIndex - 1 < 0) audioFiles.lastIndex else currentIndex - 1
                            currentlyPlayingAudio = audioFiles[previousIndex]
                            playAudio(audioFiles[previousIndex].uri, exoPlayer)
                        }
                    },
                    onDismiss = { isFullScreen = false } // Exit fullscreen
                )
            }
        }

        // Bottom Player (only shown when not in fullscreen)
        if (!isFullScreen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(20.dp))
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .background(MaterialTheme.colorScheme.primary) // Background color for the player
                    .clickable { isFullScreen = true } // Toggle fullscreen on click
            ) {
                BottomNowPlaying(
                    audioFile = currentlyPlayingAudio,
                    exoPlayer = exoPlayer,
                    onNext = {
                        val currentIndex = audioFiles.indexOf(currentlyPlayingAudio)
                        if (currentIndex != -1) {
                            val nextIndex = (currentIndex + 1) % audioFiles.size
                            currentlyPlayingAudio = audioFiles[nextIndex]
                            playAudio(audioFiles[nextIndex].uri, exoPlayer)
                        }
                    },
                    onPrevious = {
                        val currentIndex = audioFiles.indexOf(currentlyPlayingAudio)
                        if (currentIndex != -1) {
                            val previousIndex =
                                if (currentIndex - 1 < 0) audioFiles.lastIndex else currentIndex - 1
                            currentlyPlayingAudio = audioFiles[previousIndex]
                            playAudio(audioFiles[previousIndex].uri, exoPlayer)
                        }

                    }
                )
            }
        }

    }
}