 /**data class AudioFile(
    val uri: Uri,
    val title: String,
    val artist: String?,
    val duration: Long
)
fun loadAudioFiles(contentResolver: android.content.ContentResolver): List<AudioFile> {
    val audioList = mutableListOf<AudioFile>()
    val collectionsons =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }


    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION
    )


    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
    val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"


    val cursor: Cursor? = contentResolver.query(
        uri,
        projection,
        selection,
        null,
        MediaStore.Audio.Media.TITLE + " ASC"
    )
    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val title = it.getString(titleColumn)
            val artist = it.getString(artistColumn)
            val duration = it.getLong(durationColumn)

            val contentUri = Uri.withAppendedPath(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString()
            )

            audioList.add(AudioFile(contentUri, title, artist, duration))
        }
    }

    return audioList
}**/
