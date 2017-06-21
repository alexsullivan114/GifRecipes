package alexsullivan.gifrecipes;

import alexsullivan.gifrecipes.GifRecipeViewerActivity.Creator.URL_KEY
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.layout_gif_recipe_viewer.*


class GifRecipeViewerActivity : BaseActivity<GifRecipeViewerViewState>() {

    object Creator {

        val URL_KEY = "URL_KEY"

        fun start(context: Context, url: String) {
            val intent = Intent(context, GifRecipeViewerActivity::class.java).putExtra(URL_KEY, url)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_gif_recipe_viewer)
    }

    override val presenter by lazy {
        GifRecipeViewerPresenter.create(intent.getStringExtra(URL_KEY))
    }

    override fun accept(viewState: GifRecipeViewerViewState) {
        when (viewState) {
            is GifRecipeViewerViewState.StaticImage -> {
                placeholder.setImageBitmap(viewState.image)
            }
        }
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented")
    }
}