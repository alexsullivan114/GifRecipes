package alexsullivan.gifrecipes.categoryselection;


import alexsullivan.gifrecipes.BaseActivity
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.animations.ZoomOutPageTransformer
import android.os.Bundle
import com.alexsullivan.GifRecipeRepository
import kotlinx.android.synthetic.main.layout_category.*

class CategorySelectionActivity : BaseActivity<CategorySelectionViewState>() {
    override val presenter by lazy {
        CategorySelectionPresenter.create(GifRecipeRepository.default)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_category)
        pager.setPadding(200, 0, 200, 0)
        pager.clipToPadding = false
        pager.pageMargin = 100
        pager.setPageTransformer(true, ZoomOutPageTransformer())
        pager.offscreenPageLimit = 5
    }

    override fun accept(viewState: CategorySelectionViewState) {
        when(viewState) {
            is CategorySelectionViewState.GifList -> {
                pager.adapter = HotRecipesPagerAdapter(viewState.gifRecipes)
            }
        }
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented")
    }
}