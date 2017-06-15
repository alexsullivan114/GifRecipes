package alexsullivan.gifrecipes.RecipesList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.MediaController
import com.alexsullivan.ImageType
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ProgressBarDrawable
import kotlinx.android.synthetic.main.recipe_item.view.*

class GifRecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    fun setUrl(url: String, imageType: ImageType) {
        if (imageType == ImageType.GIF) {
            itemView.drawee.visibility = View.VISIBLE
            itemView.video.visibility = View.GONE
            setImageUrl(url)
        }
        else {
            itemView.drawee.visibility = View.GONE
            itemView.video.visibility = View.VISIBLE
            setVideoUrl(url)
        }
    }

    private fun setVideoUrl(url: String) {
        val videoMediaController = MediaController(itemView.context)
        videoMediaController.setMediaPlayer(itemView.video)
        itemView.video.apply {
            setVideoPath(url)
            requestFocus()
            start()
        }
    }

    private fun setImageUrl(url: String) {
        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(url)
                .setAutoPlayAnimations(true)
                .build()

        itemView.drawee.controller = controller
        itemView.drawee.hierarchy.setProgressBarImage(ProgressBarDrawable())
    }
}