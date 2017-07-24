package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.GifRecipeViewerActivity
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.utils.addInfiniteScrollListener
import alexsullivan.gifrecipes.utils.gone
import alexsullivan.gifrecipes.utils.visible
import alexsullivan.gifrecipes.viewarchitecture.BaseFragment
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alexsullivan.GifRecipeRepository
import kotlinx.android.synthetic.main.layout_recipe_category_list.*
import kotlinx.android.synthetic.main.layout_recipe_category_list.view.*


class RecipeCategoryListFragment : BaseFragment<RecipeCategoryListViewState, RecipeCategoryListPresenter>(),
    RecipeCategoryListAdapter.ClickCallback {

    companion object {
        val SEARCH_KEY = "Search Key"
        fun build(searchTerm: String): RecipeCategoryListFragment {
            val args = Bundle()
            args.putString(SEARCH_KEY, searchTerm)
            val fragment = RecipeCategoryListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initPresenter(): RecipeCategoryListPresenter {
        val searchTerm = arguments.getString(SEARCH_KEY)
        return RecipeCategoryListPresenter.create(searchTerm, GifRecipeRepository.default)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_recipe_category_list, container, false)
        view.list.layoutManager = LinearLayoutManager(context)
        view.list.addInfiniteScrollListener {
            presenter.reachedBottom()
        }
        return view
    }

    override fun accept(viewState: RecipeCategoryListViewState) {
        when (viewState) {
            is RecipeCategoryListViewState.Loading -> {
                loading.visible()
                list.gone()
            }
            is RecipeCategoryListViewState.RecipeList -> {
                if (list.adapter == null) {
                    list.adapter = RecipeCategoryListAdapter(viewState.recipes, this)
                } else {
                    val adapter = list.adapter as RecipeCategoryListAdapter
                    adapter.showBottomLoading = false
                    adapter.gifList = viewState.recipes
                }
                loading.gone()
                list.visible()
            }
            is RecipeCategoryListViewState.LoadingMore -> {
                (list.adapter as RecipeCategoryListAdapter).showBottomLoading = true
            }
        }
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented")
    }

    override fun recipeClicked(recipe: GifRecipeUI, view: View) {
        val imagePair = Pair(view, getString(R.string.recipe_transition_image_name))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imagePair)
        val intent = GifRecipeViewerActivity.IntentFactory.build(activity, recipe)
        startActivity(intent, options.toBundle())
    }
}