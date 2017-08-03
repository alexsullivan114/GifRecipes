package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.GifRecipeViewerActivity
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.database.RoomRecipeDatabaseHolder
import alexsullivan.gifrecipes.search.SearchProvider
import alexsullivan.gifrecipes.utils.*
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
        return RecipeCategoryListPresenter.create(searchTerm, GifRecipeRepository.default,
                RoomRecipeDatabaseHolder.get(context.applicationContext))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is SearchProvider) {
            presenter.setSearchTermSource((activity as SearchProvider).getObservableSource())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_recipe_category_list, container, false)
        view.list.layoutManager = LinearLayoutManager(context)
        view.list.adapter = RecipeCategoryListAdapter(listOf(), this)
        view.list.addInfiniteScrollListener {
            presenter.reachedBottom()
        }
        return view
    }

    override fun accept(viewState: RecipeCategoryListViewState) {
        when (viewState) {
            is RecipeCategoryListViewState.Loading -> {
                val adapter = list.castedAdapter(RecipeCategoryListAdapter::class.java)
                adapter.gifList = listOf()
                loading.visible()
                list.invisible()
            }
            is RecipeCategoryListViewState.RecipeList -> {
                val adapter = list.castedAdapter(RecipeCategoryListAdapter::class.java)
                adapter.showBottomLoading = false
                adapter.gifList = viewState.recipes
                loading.gone()
                list.visible()
            }
            is RecipeCategoryListViewState.LoadingMore -> {
                (list.adapter as RecipeCategoryListAdapter).showBottomLoading = true
            }
            is RecipeCategoryListViewState.NetworkError -> {
                list.gone()
                loading.gone()
                error.visible()
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

    override fun recipeFavoriteToggled(recipe: GifRecipeUI) {
        presenter.recipeFavoriteToggled(recipe)
    }
}