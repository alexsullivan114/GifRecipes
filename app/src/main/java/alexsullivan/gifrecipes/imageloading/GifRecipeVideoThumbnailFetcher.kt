package alexsullivan.gifrecipes.imageloading

import alexsullivan.gifrecipes.utils.firstFrame
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.alexsullivan.GifRecipe
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


class GifRecipeVideoThumbnailFetcher(val gifRecipe: GifRecipe): DataFetcher<InputStream> {
    override fun cancel() {
        //TODO
    }

    override fun getDataSource() = DataSource.REMOTE
    override fun getDataClass() = InputStream::class.java

    override fun cleanup() {
        // no cleanup to do, all handled in loaddata
    }

    override fun loadData(priority: Priority?, callback: DataFetcher.DataCallback<in InputStream>) {
        // TODO: Like triple memory usage here, work on it.
        val bitmap: Bitmap = MediaMetadataRetriever().firstFrame(gifRecipe.url)
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val inputStream = ByteArrayInputStream(byteArray)
            callback.onDataReady(inputStream)
        } catch (e: IOException) {
            callback.onLoadFailed(e)
        } finally {
            bitmap.recycle()
        }
    }
}