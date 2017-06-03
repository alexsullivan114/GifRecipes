package alexsullivan.gifrecipes.RecipesList

import android.graphics.drawable.Animatable
import android.support.v7.widget.RecyclerView
import android.view.View
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.ControllerListener
import com.facebook.drawee.drawable.ProgressBarDrawable
import com.facebook.imagepipeline.image.ImageInfo
import kotlinx.android.synthetic.main.recipe_item.view.*

class GifRecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    fun setUrl(url: String) {
        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(url)
                .setAutoPlayAnimations(true)
                .setControllerListener(object: ControllerListener<ImageInfo>{
                    override fun onIntermediateImageFailed(id: String?, throwable: Throwable?) {
                        print("ff")
                    }

                    override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
                        print("ff")
                    }

                    override fun onSubmit(id: String?, callerContext: Any?) {
                        print("ff")
                    }

                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        print("ff")
                    }

                    override fun onRelease(id: String?) {
                        print("ff")
                    }

                    override fun onFailure(id: String?, throwable: Throwable?) {
                        print("ff")
                    }
                })
                .build();

        itemView.drawee.controller = controller
        itemView.drawee.hierarchy.setProgressBarImage(ProgressBarDrawable())
    }
}