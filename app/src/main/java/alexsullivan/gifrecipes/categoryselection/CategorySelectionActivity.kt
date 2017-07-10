package alexsullivan.gifrecipes.categoryselection;


import alexsullivan.gifrecipes.BaseActivity
import alexsullivan.gifrecipes.GifRecipeViewerActivity
import alexsullivan.gifrecipes.R
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.view.View
import com.alexsullivan.GifRecipeRepository
import kotlinx.android.synthetic.main.layout_category.*



class CategorySelectionActivity : BaseActivity<CategorySelectionViewState>(), HotRecipeAdapterCallback {
    override val presenter by lazy {
        CategorySelectionPresenter.create(GifRecipeRepository.default)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_category)
        pager.setPadding(200, 0, 200, 0)
        pager.clipToPadding = false
        pager.setPageTransformer(true, HotGifPageTransformer())
        pager.offscreenPageLimit = 5
    }

    override fun accept(viewState: CategorySelectionViewState) {
        when(viewState) {
            is CategorySelectionViewState.GifList -> {
                pager.adapter = HotRecipesPagerAdapter(viewState.gifRecipes, this)
                pager.animate().alpha(1f).start()
                progressBar.visibility = View.GONE
            }
            is CategorySelectionViewState.FetchingGifs -> {
                pager.alpha = 0f
                progressBar.visibility = View.VISIBLE
            }
        }
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented")
    }

    override fun recipeClicked(hotGifRecipeItem: HotGifRecipeItem, previewImage: View) {
        presenter.recipeClicked(hotGifRecipeItem)
        val imagePair = Pair(previewImage, getString(R.string.recipe_transition_image_name))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imagePair)
        val intent = GifRecipeViewerActivity.IntentFactory.build(this, hotGifRecipeItem.link, hotGifRecipeItem.title, hotGifRecipeItem.imageType)
        startActivity(intent, options.toBundle())
    }
}