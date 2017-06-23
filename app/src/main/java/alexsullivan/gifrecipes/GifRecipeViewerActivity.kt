package alexsullivan.gifrecipes;

import alexsullivan.gifrecipes.GifRecipeViewerActivity.IntentFactory.TITLE_KEY
import alexsullivan.gifrecipes.GifRecipeViewerActivity.IntentFactory.URL_KEY
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.layout_gif_recipe_viewer.*


class GifRecipeViewerActivity : BaseActivity<GifRecipeViewerViewState>() {

    object IntentFactory {

        val URL_KEY = "URL_KEY"
        val TITLE_KEY = "TITLE_KEY"

        fun build(context: Context, url: String, title: String): Intent {
            val intent = Intent(context, GifRecipeViewerActivity::class.java)
                    .putExtra(URL_KEY, url)
                    .putExtra(TITLE_KEY, title)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_gif_recipe_viewer)
    }

    override val presenter by lazy {
        GifRecipeViewerPresenter.create(intent.getStringExtra(URL_KEY),
                intent.getStringExtra(TITLE_KEY))
    }

    override fun accept(viewState: GifRecipeViewerViewState) {
        when (viewState) {
            is GifRecipeViewerViewState.Playing -> {
                placeholder.setImageBitmap(viewState.image)
                recipeTitle.text = viewState.title
            }
        }
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented")
    }
}