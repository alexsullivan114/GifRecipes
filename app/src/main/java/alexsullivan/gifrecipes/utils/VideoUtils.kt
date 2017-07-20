package alexsullivan.gifrecipes.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever

fun MediaMetadataRetriever.firstFrame(videoUrl: String): Bitmap {
    setDataSource(videoUrl, HashMap<String, String>())
    val map = getFrameAtTime(0)
    release()
    return map
}