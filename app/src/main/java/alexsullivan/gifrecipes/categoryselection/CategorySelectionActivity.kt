package alexsullivan.gifrecipes.categoryselection

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.GifRecipeViewerActivity
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.application.AndroidLogger
import alexsullivan.gifrecipes.recipelist.RecipeCategoryContainerActivity
import alexsullivan.gifrecipes.recipelist.RecipeListIndicatorAdapter
import alexsullivan.gifrecipes.recipelist.SelectedIndexCallback
import alexsullivan.gifrecipes.recipelist.indexFromCategory
import alexsullivan.gifrecipes.utils.*
import alexsullivan.gifrecipes.viewarchitecture.BaseActivity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.SharedElementCallback
import android.support.v4.util.Pair
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.alexsullivan.GifRecipeRepository
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_category.*

class CategorySelectionActivity : BaseActivity<CategorySelectionViewState, CategorySelectionPresenter>(),
                                  RecipeAdapterCallback, SelectedIndexCallback {

    override fun initPresenter() = CategorySelectionPresenter.create(GifRecipeRepository.default,
        Schedulers.io(), AndroidLogger)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_category)
        pager.setPadding(200, 0, 200, 0)
        pager.clipToPadding = false
        pager.setPageTransformer(true, HotGifPageTransformer())
        pager.offscreenPageLimit = 5
        recyclerView.layoutManager = GridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = RecipeListIndicatorAdapter(this, recyclerView.layoutManager, false)
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
        // We need to make sure that the item we want to do a shared element transition on
        // is actually on the screen - so we need to scroll there and wait for a layout pass.
        postponeEnterTransition()
        recyclerView.scrollToPosition(indexFromCategory(updatedCategory))
        recyclerView.waitForLayout {
            startPostponedEnterTransition()
        }
        // Set our shared element callback so that we can re-route whatever is being mapped to the
        // category the user left the last screen on.
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
                progressBar.gone()
            }
            is CategorySelectionViewState.FetchingGifs -> {
                pager.alpha = 0f
                progressBar.visible()
            }
            is CategorySelectionViewState.NetworkError -> {
                pager.gone()
                progressBar.gone()
                error.visible()
            }
        }
    }

    override fun recipeClicked(gifRecipe: GifRecipeUI, previewImage: View) {
        val imagePair = Pair(previewImage, getString(R.string.recipe_transition_image_name))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imagePair)
        val intent = GifRecipeViewerActivity.IntentFactory.build(this, gifRecipe)
        startActivity(intent, options.toBundle())
    }

    override fun categorySelected(category: Category) {
        categoryClicked(category)
    }

    private fun categoryClicked(category: Category) {
        val holder = recyclerView.castedViewHolderAtPosition<RecipeListIndicatorAdapter.RecipeListIndicatorViewHolder>(indexFromCategory(category))
        val imagePair: Pair<View, String> = Pair(holder.image, str(category.transitionName))
        val options = makeSceneTransitionWithNav(this, imagePair)
        startActivity(RecipeCategoryContainerActivity.buildIntent(this, category), options.toBundle())
    }

    private fun viewForCategory(category: Category): View {
        val viewholder = recyclerView.castedViewHolderAtPosition<RecipeListIndicatorAdapter.RecipeListIndicatorViewHolder>(indexFromCategory(category))
        return viewholder.image
    }
}