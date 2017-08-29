package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.GifRecipeViewerActivity
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.database.RoomRecipeDatabaseHolder
import alexsullivan.gifrecipes.favoriting.FavoriteGifRecipeRepository
import alexsullivan.gifrecipes.favoriting.RoomFavoriteCache
import alexsullivan.gifrecipes.search.SearchProvider
import alexsullivan.gifrecipes.utils.*
import alexsullivan.gifrecipes.viewarchitecture.BaseFragment
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alexsullivan.GifRecipeRepository
import kotlinx.android.synthetic.main.layout_recipe_category_list.*
import kotlinx.android.synthetic.main.layout_recipe_category_list.view.*


class RecipeCategoryListFragment : BaseFragment<RecipeCategoryListViewState, RecipeCategoryListPresenter>(),
    RecipeCategoryListAdapter.ClickCallback {

    private var savedListState: Parcelable? = null

    companion object {
        val SEARCH_KEY = "Search Key"
        val LIST_SAVE_STATE = "List Save Instance State"
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
        val dao = RoomRecipeDatabaseHolder.get(context.applicationContext).gifRecipeDao()
        val cache = RoomFavoriteCache.getInstance(dao)
        val presenter = if(searchTerm == str(Category.FAVORITE.displayName)) {
            val repo = FavoriteGifRecipeRepository(dao)
            FavoriteRecipeListPresenter(repo, cache)
        } else {
            RecipeCategoryListPresenterImpl(searchTerm, GifRecipeRepository.default, cache)
        }
        return presenter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? SearchProvider)?.let {
            presenter.setSearchTermSource(it.getObservableSource())
        }

        savedInstanceState?.let {
            savedListState = savedInstanceState.getParcelable(LIST_SAVE_STATE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_recipe_category_list, container, false)
        view.list.layoutManager = LinearLayoutManager(context)
        view.list.adapter = RecipeCategoryListAdapter(listOf(), this)
        ((view.list.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
        view.list.addInfiniteScrollListener {
            presenter.reachedBottom()
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val state = list.layoutManager.onSaveInstanceState()
        outState?.putParcelable(LIST_SAVE_STATE, state)
    }

    override fun accept(viewState: RecipeCategoryListViewState) {
        when (viewState) {
            is RecipeCategoryListViewState.Loading -> {
                val adapter = list.castedAdapter<RecipeCategoryListAdapter>()
                updateGifList(listOf())
                loading.visible()
                empty.gone()
                list.invisible()
            }
            is RecipeCategoryListViewState.RecipeList -> {
                val adapter = list.castedAdapter<RecipeCategoryListAdapter>()
                adapter.showBottomLoading = false
                updateGifList(viewState.recipes)
                loading.gone()
                empty.gone()
                list.visible()
                viewState.recipes.emptyLet {
                    empty.visible()
                    list.gone()
                }
            }
            is RecipeCategoryListViewState.LoadingMore -> {
                val adapter = list.castedAdapter<RecipeCategoryListAdapter>()
                updateGifList(viewState.recipes)
                adapter.showBottomLoading = true
            }
            is RecipeCategoryListViewState.NetworkError -> {
                list.gone()
                loading.gone()
                empty.gone()
                error.visible()
            }
        }
    }

    override fun recipeClicked(recipe: GifRecipeUI, view: View) {
        val imagePair = Pair(view, getString(R.string.recipe_transition_image_name))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imagePair)
        val intent = GifRecipeViewerActivity.IntentFactory.build(activity, recipe)
        startActivity(intent, options.toBundle())
    }

    override fun recipeShareClicked(recipe: GifRecipeUI) {
        recipe.url.ifPresent(activity::shareRecipe)
    }

    override fun recipeFavoriteToggled(recipe: GifRecipeUI) {
        presenter.recipeFavoriteToggled(recipe)
    }

    private fun updateGifList(reciepList: List<GifRecipeUI>) {
        val adapter = list.castedAdapter<RecipeCategoryListAdapter>()
        adapter.gifList = reciepList
        savedListState?.let {
            list.layoutManager?.onRestoreInstanceState(it)
            savedListState = null
        }
    }
}