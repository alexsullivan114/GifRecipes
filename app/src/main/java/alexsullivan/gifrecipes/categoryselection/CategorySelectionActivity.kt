package alexsullivan.gifrecipes.categoryselection;


import alexsullivan.gifrecipes.BaseActivity
import alexsullivan.gifrecipes.GifRecipeViewerActivity
import alexsullivan.gifrecipes.R
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
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
        pager.setPadding(150, 0, 150, 0)
        pager.clipToPadding = false
        pager.pageMargin = 25
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
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, previewImage, getString(R.string.recipe_transition_name))
        val intent = GifRecipeViewerActivity.IntentFactory.build(this, hotGifRecipeItem.link)
        startActivity(intent, options.toBundle())
    }
}