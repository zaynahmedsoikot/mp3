package com.example.Mp3PlayerAddsFree

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.Mp3PlayerAddsFree.services.PlaybackService
import com.example.Mp3PlayerAddsFree.ui.theme.LAAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val intent = Intent(this, PlaybackService::class.java)
        ContextCompat.startForegroundService(this, intent)




        super.onCreate(savedInstanceState)
        setContent{
            LAAppTheme {
                AudioFilePermissionRequest()
            }
        }
    }

}




@Composable
fun AudioFilePermissionRequest() {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasPermission) {


            AudioFileList(contentResolver = contentResolver )
        } else {
            Text(text = "Permission is required to access audio files.", color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { permissionLauncher.launch(permission) }) {
                Text("Request Permission")
            }
        }
    }
}










data class AudioFile(
    val uri: Uri,
    val title: String,
    val duration: Long,
    val albumArt: Uri? = null,
    val artist: String// For song images
)




fun loadAudioFiles(contentResolver: ContentResolver): List<AudioFile> {

    val audioFiles = mutableListOf<AudioFile>()

    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ALBUM_ID // Album ID to fetch album art
    )

    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
    val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

    contentResolver.query(
        collection,
        projection,
        selection,
        null,
        sortOrder
    )?.use { cursor ->
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

        while (cursor.moveToNext()) {
            val title = cursor.getString(titleColumn)
            val artist = cursor.getString(artistColumn)
            val id = cursor.getLong(idColumn)
            val duration = cursor.getLong(durationColumn)
            val albumId = cursor.getLong(albumIdColumn)
            val contentUri = Uri.withAppendedPath(collection, id.toString())

            val albumArtUri = Uri.withAppendedPath(
                Uri.parse("content://media/external/audio/albumart"),
                albumId.toString()
            )

            audioFiles.add(AudioFile(contentUri, title, duration, albumArtUri, artist))
        }
    }

    return audioFiles

}





@Composable
fun MarqueeText(
    text: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    durationMillis: Int = 6000
) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = text.length * 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = modifier
        .clipToBounds()
        ) {
        Text(
            text = text,
            modifier = Modifier
                .offset(x = offsetX.dp),
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Clip // Ensure the text clips properly
        )
    }
}

