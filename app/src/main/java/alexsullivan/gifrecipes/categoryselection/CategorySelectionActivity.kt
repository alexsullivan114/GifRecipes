package alexsullivan.gifrecipes.categoryselection

import alexsullivan.gifrecipes.viewarchitecture.BaseActivity
import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.GifRecipeViewerActivity
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.recipelist.RecipeCategoryContainerActivity
import alexsullivan.gifrecipes.utils.makeSceneTransitionWithNav
import alexsullivan.gifrecipes.utils.str
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.SharedElementCallback
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

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        // Not sure this will ever happen - but bail if it does.
        if (data == null) return
        val originalExtraKey = str(R.string.category_original_extra_key)
        val updatedExtraKey = str(R.string.category_extra_key)
        // If we don't have a new and/or original category there's nothing to do so bail.
        if (!data.hasExtra(originalExtraKey) || !data.hasExtra(updatedExtraKey)) return
        val updatedCategory = data.getSerializableExtra(updatedExtraKey) as Category
        val originalCategory = data.getSerializableExtra(originalExtraKey) as Category
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                if (updatedCategory != originalCategory) {
                    //Add our new element to our transition
                    sharedElements?.put(getString(updatedCategory.transitionName), viewForCategory(updatedCategory))
                    sharedElements?.remove(str(originalCategory.transitionName))
                    names?.add(str(updatedCategory.transitionName))
                    names?.remove(str(originalCategory.transitionName))
                }
                //Remove this callback so we don't get hit when we come back and start again.
                this@CategorySelectionActivity.setExitSharedElementCallback(null as SharedElementCallback?)
            }
        })
    }

    override fun accept(viewState: CategorySelectionViewState) {
        when (viewState) {
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

    fun categoryClicked(view: View, category: Category) {
        val imagePair = Pair(view, view.transitionName)
        val options = makeSceneTransitionWithNav(this, imagePair)
        startActivity(RecipeCategoryContainerActivity.buildIntent(this, category), options.toBundle())
    }

    private fun bindCategories() {
        dessert.setImageResource(Category.DESSERT.iconRes)
        vegan.setImageResource(Category.VEGAN.iconRes)
        vegetarian.setImageResource(Category.VEGETARIAN.iconRes)
        chicken.setImageResource(Category.CHICKEN.iconRes)
        pork.setImageResource(Category.PORK.iconRes)
        salmon.setImageResource(Category.SALMON.iconRes)

        dessert.setOnClickListener { categoryClicked(it, Category.DESSERT) }
        vegan.setOnClickListener { categoryClicked(it, Category.VEGAN) }
        vegetarian.setOnClickListener { categoryClicked(it, Category.VEGETARIAN) }
        chicken.setOnClickListener { categoryClicked(it, Category.CHICKEN) }
        pork.setOnClickListener { categoryClicked(it, Category.PORK) }
        salmon.setOnClickListener { categoryClicked(it, Category.SALMON) }
    }

    private fun viewForCategory(category: Category): View {
        return when (category) {
            Category.DESSERT -> dessert
            Category.VEGAN -> vegan
            Category.VEGETARIAN -> vegetarian
            Category.CHICKEN -> chicken
            Category.PORK -> pork
            Category.SALMON -> salmon
        }
    }
}