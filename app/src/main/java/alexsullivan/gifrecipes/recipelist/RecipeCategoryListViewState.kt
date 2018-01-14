package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import android.arch.paging.PagedList
import com.alexsullivan.GifRecipe

/**
 * Created by Alexs on 8/21/2017.
 */
sealed class RecipeCategoryListViewState : ViewState {
    class Loading : RecipeCategoryListViewState()
    class LoadingMore(val recipes: List<GifRecipeUI>): RecipeCategoryListViewState()
    class RecipeList(val recipes: MutableList<GifRecipeUI>): RecipeCategoryListViewState()
    class LoadMoreError(val recipes: MutableList<GifRecipeUI>): RecipeCategoryListViewState()
    class NetworkError : RecipeCategoryListViewState()
    class Favorited(val isFavorite: Boolean, val recipe: GifRecipe): RecipeCategoryListViewState()
    class PagingList(val list: PagedList<GifRecipeUI>): RecipeCategoryListViewState()
}