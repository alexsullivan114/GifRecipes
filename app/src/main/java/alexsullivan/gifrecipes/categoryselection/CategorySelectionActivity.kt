package alexsullivan.gifrecipes.categoryselection


import alexsullivan.gifrecipes.BaseActivity
import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.GifRecipeViewerActivity
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.recipelist.RecipesListActivity
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.view.View
import com.alexsullivan.GifRecipeRepository
import kotlinx.android.synthetic.main.layout_category.*



class CategorySelectionActivity : BaseActivity<CategorySelectionViewState, CategorySelectionPresenter>(), HotRecipeAdapterCallback {

    override fun initPresenter() = CategorySelectionPresenter.create(GifRecipeRepository.default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_category)
        pager.setPadding(200, 0, 200, 0)
        pager.clipToPadding = false
        pager.setPageTransformer(true, HotGifPageTransformer())
        pager.offscreenPageLimit = 5
        bindCategories()
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
        val intent = GifRecipeViewerActivity.IntentFactory.build(this, hotGifRecipeItem.link, hotGifRecipeItem.imageType)
        startActivity(intent, options.toBundle())
    }

    fun categoryClicked(view: View) {
        val imagePair = Pair(view, view.transitionName)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imagePair)
        startActivity(RecipesListActivity.buildIntent(this), options.toBundle())
    }

    private fun bindCategories() {
        dessert.setImageResource(Category.DESSERT.iconRes)
        vegan.setImageResource(Category.VEGAN.iconRes)
        vegetarian.setImageResource(Category.VEGETARIAN.iconRes)
        chicken.setImageResource(Category.CHICKEN.iconRes)
        pork.setImageResource(Category.PORK.iconRes)
        salmon.setImageResource(Category.SALMON.iconRes)

        dessert.setOnClickListener(this::categoryClicked)
        vegan.setOnClickListener(this::categoryClicked)
        vegetarian.setOnClickListener(this::categoryClicked)
        chicken.setOnClickListener(this::categoryClicked)
        pork.setOnClickListener(this::categoryClicked)
        salmon.setOnClickListener(this::categoryClicked)
    }
}