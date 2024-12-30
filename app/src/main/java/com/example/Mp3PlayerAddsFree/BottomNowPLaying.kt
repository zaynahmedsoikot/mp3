package com.example.Mp3PlayerAddsFree

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.rememberAsyncImagePainter


@Composable
fun BottomNowPlaying(
    audioFile: AudioFile?,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier) {





    var currentPosition by remember { mutableLongStateOf(0L) }
    val duration = audioFile?.duration ?: 0L
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }

    // Format milliseconds to mm:ss
    fun formatTime(ms: Long): String {
        val minutes = ms / 1000 / 60
        val seconds = (ms / 1000) % 60
        return   String.format("%02d:%02d", minutes, seconds)
    }

    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                isPlaying = isPlayingState
            }
        })
        while (true) {
            currentPosition = exoPlayer.currentPosition
            kotlinx.coroutines.delay(500)
        }
    }


        if (audioFile != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(MaterialTheme.colorScheme.inversePrimary)
                    .padding(horizontal = 16.dp)
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
                        .size(50.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Scrolling Song Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    MarqueeText(
                        text = "${audioFile.title} - ${audioFile.artist}",
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(top = 3.dp)
                    ) {
                        fun formatDuration(durationMillis: Long): String {
                            val minutes = (durationMillis / 1000) / 60
                            val seconds = (durationMillis / 1000) % 60
                            return String.format("%02d:%02d", minutes, seconds)
                        }
                        // Display current time and duration
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Current Time
                            Text(
                                text = formatDuration(currentPosition),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                            // Total Duration
                            Text(
                                text = formatDuration(duration),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Slider for progress
                        Slider(
                            modifier = Modifier.padding(top = 5.dp),
                            value = currentPosition.toFloat(),
                            valueRange = 0f..duration.toFloat(),
                            onValueChange = { newValue ->
                                currentPosition = newValue.toLong()
                                exoPlayer.seekTo(currentPosition)
                            },
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }



                    // Playback Controls
                    Row(modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.previous_24),
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier
                                .size(35.dp)
                                .clickable { onPrevious() }
                        )

                        Icon(
                            painter = if (exoPlayer.isPlaying) painterResource(id = R.drawable.pause_24)
                            else painterResource(id = R.drawable.play_arrow_24),
                            contentDescription = if (exoPlayer.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier
                                .size(45.dp)
                                .clickable {if(isPlaying) exoPlayer.pause() else exoPlayer.play()
                                }
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.next_24),
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier
                                .size(35.dp)
                                .clickable { onNext() }
                        )
                }
            }
        }
    }
}