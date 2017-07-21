package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.GifRecipeViewerActivity
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.utils.gone
import alexsullivan.gifrecipes.utils.str
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
        val CATEGORY_KEY = "Category Key"
        fun build(category: Category): RecipeCategoryListFragment {
            val args = Bundle()
            args.putSerializable(CATEGORY_KEY, category)
            val fragment = RecipeCategoryListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initPresenter(): RecipeCategoryListPresenter {
        val category = arguments.getSerializable(CATEGORY_KEY) as Category
        return RecipeCategoryListPresenter.create(category,
                str(category.displayName), GifRecipeRepository.default)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_recipe_category_list, container, false)
        view.list.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun accept(viewState: RecipeCategoryListViewState) {
        when (viewState) {
            is RecipeCategoryListViewState.Loading -> {
                loading.visible()
                list.gone()
            }
            is RecipeCategoryListViewState.RecipeList -> {
                list.adapter = RecipeCategoryListAdapter(viewState.recipes, this)
                loading.gone()
                list.visible()
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