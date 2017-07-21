package alexsullivan.gifrecipes.imageloading

import alexsullivan.gifrecipes.GifRecipeUI
import com.alexsullivan.ImageType
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.data.HttpUrlFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.model.stream.HttpGlideUrlLoader
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class GifRecipeLoader : ModelLoader<GifRecipeUI, InputStream>{

    object Factory: ModelLoaderFactory<GifRecipeUI, InputStream> {
        override fun teardown() {}

        override fun build(multiFactory: MultiModelLoaderFactory?): ModelLoader<GifRecipeUI, InputStream> {
            return GifRecipeLoader()
        }
    }

    override fun buildLoadData(model: GifRecipeUI, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream>? {
        var dataFetcher: DataFetcher<InputStream> = GifRecipeVideoThumbnailFetcher(model)
        if (model.imageType == ImageType.GIF) {
            dataFetcher = HttpUrlFetcher(GlideUrl(model.thumbnail), options.get(HttpGlideUrlLoader.TIMEOUT))
        }
        return ModelLoader.LoadData(ObjectKey(model.url), dataFetcher)
    }

    override fun handles(model: GifRecipeUI?) = true
}