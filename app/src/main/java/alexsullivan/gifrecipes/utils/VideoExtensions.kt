package alexsullivan.gifrecipes.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.view.TextureView

fun MediaMetadataRetriever.firstFrame(videoUrl: String): Bitmap {
    setDataSource(videoUrl, HashMap<String, String>())
    val map = getFrameAtTime(0)
    release()
    return map
}

fun TextureView.adjustAspectRatio(videoWidth: Int, videoHeight: Int) {
    val viewWidth = width
    val viewHeight = height
    val aspectRatio = videoHeight.toDouble() / videoWidth

    val newWidth: Int
    val newHeight: Int
    if (viewHeight > (viewWidth * aspectRatio).toInt()) {
        // limited by narrow width; restrict height
        newWidth = viewWidth
        newHeight = (viewWidth * aspectRatio).toInt()
    } else {
        // limited by short height; restrict width
        newWidth = (viewHeight / aspectRatio).toInt()
        newHeight = viewHeight
    }
    val xoff = (viewWidth - newWidth) / 2f
    val yoff = (viewHeight - newHeight) / 2f

    val txform = Matrix()
    getTransform(txform)
    txform.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
    txform.postTranslate(xoff, yoff)
    setTransform(txform)
}