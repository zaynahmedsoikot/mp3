package com.example.Mp3PlayerAddsFree

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult

@Composable
fun FullScreenPlayer(
    audioFile: AudioFile?,
    exoPlayer: ExoPlayer,
    onDismiss: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {

    data class AudioFile(
        val title: String,
        val artist: String,
        val albumArt: String?, // URL or URI of the album art
        val duration: Long
    )



    // Song Duration Slider
    var currentPosition by remember { mutableStateOf(0L) }
    val duration = audioFile?.duration ?: 0L
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }


    val defaultBackground = MaterialTheme.colorScheme.background
    val dominantColor = remember { mutableStateOf(defaultBackground) }

    // Format milliseconds to mm:ss
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
    val context = LocalContext.current

    LaunchedEffect(audioFile?.albumArt) {
        if (audioFile?.albumArt != null) {

            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(audioFile.albumArt)
                .allowHardware(false) // Needed for Palette
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable?.toBitmap()

            if (result != null) {
                Palette.from(result).generate { palette ->
                    val color = palette?.getDominantColor(defaultBackground.toArgb())
                        ?: defaultBackground.toArgb()
                    dominantColor.value = Color(color)
                }
            }
        }
    }


        Column(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)

        ) {

            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), Alignment.TopEnd) {
                Icon(
                    painter = painterResource(R.drawable.baseline_close_24),
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.inversePrimary,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(30.dp)
                        .clickable { onDismiss() }
                )
            }

            if (audioFile != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally)

                ) {
                    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                    val albumArtSize = screenWidth * 1f

                    Crossfade(
                        targetState = audioFile.albumArt,
                        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
                    ) { albumArt ->


                        androidx.compose.foundation.Image(
                            painter = rememberAsyncImagePainter(
                                model = audioFile.albumArt ?: R.drawable.emptysong,
                                placeholder = painterResource(id = R.drawable.emptysong),
                                error = painterResource(id = R.drawable.emptysong)
                            ),
                            contentDescription = "Album Art",
                            modifier = Modifier
                                .size(albumArtSize)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                        )
                    }

                }








                Column(modifier = Modifier.padding(top = 50.dp)) {
                    fun formatDuration(durationMillis: Long): String {
                        val minutes = (durationMillis / 1000) / 60
                        val seconds = (durationMillis / 1000) % 60
                        return String.format("%02d:%02d", minutes, seconds)
                    }

                    Crossfade(
                        targetState = "${audioFile.title} - ${audioFile.artist}",
                        animationSpec = tween(durationMillis = 2000),
                        label = ""
                    ) { title ->
                        MarqueeText(
                            text = title,
                            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) {
                        // Display current time and duration
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Current Time
                            Text(
                                text = formatDuration(currentPosition),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            // Total Duration
                            Text(
                                text = formatDuration(duration),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Slider for progress
                        Slider(
                            modifier = Modifier.padding(top = 16.dp),
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

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 25.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Icon(
                            painter = painterResource(id = R.drawable.previous_24),
                            contentDescription = "Previous",
                            tint = Color.LightGray,
                            modifier = Modifier
                                .size(60.dp)
                                .padding(top = 15.dp)
                                .clickable { onPrevious() }
                        )

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary, // Background color
                            modifier = Modifier
                                .padding(start = 32.dp)
                                .size(68.dp) // Adjust size for the circle
                                .clickable {
                                    if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                }
                        ) {
                            Icon(
                                painter = if (exoPlayer.isPlaying) painterResource(id = R.drawable.pause_24)
                                else painterResource(id = R.drawable.play_arrow_24),
                                contentDescription = if (exoPlayer.isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.inversePrimary,
                                modifier = Modifier
                                    .padding(15.dp)
                            )
                        }





                        Icon(
                            painter = painterResource(id = R.drawable.next_24),
                            contentDescription = "Next",
                            tint = Color.LightGray,
                            modifier = Modifier
                                .padding(start = 35.dp, top = 15.dp)
                                .size(50.dp)
                                .clickable { onNext() }
                        )



                }

            }
        }
    }
}


