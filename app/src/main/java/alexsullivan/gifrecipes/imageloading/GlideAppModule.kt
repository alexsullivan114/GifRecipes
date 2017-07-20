package alexsullivan.gifrecipes.imageloading

import android.content.Context
import com.alexsullivan.GifRecipe
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

@GlideModule
class GlideAppModule: AppGlideModule() {
    override fun registerComponents(context: Context?, registry: Registry) {
        super.registerComponents(context, registry)
        registry.append(GifRecipe::class.java, InputStream::class.java, GifRecipeLoader.Factory)
    }
}