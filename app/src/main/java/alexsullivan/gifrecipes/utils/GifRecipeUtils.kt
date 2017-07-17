package alexsullivan.gifrecipes.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.alexsullivan.GifRecipe
import com.alexsullivan.ImageType
import java.io.InputStream
import java.net.URL

fun GifRecipe.firstFrame(): Bitmap {
    val bitmap: Bitmap

    if (imageType == ImageType.VIDEO) {
        val metadataRetriever = MediaMetadataRetriever()
        bitmap = metadataRetriever.firstFrame(url)
    }
    else {
        val inputStream = URL(thumbnail).content as InputStream
        bitmap = BitmapFactory.decodeStream(inputStream)
    }

    return bitmap
}