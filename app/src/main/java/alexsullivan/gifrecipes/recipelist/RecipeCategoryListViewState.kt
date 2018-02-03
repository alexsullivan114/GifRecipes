package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import android.arch.paging.PagedList

/**
 * Created by Alexs on 8/21/2017.
 */
sealed class RecipeCategoryListViewState : ViewState {
    class Loading : RecipeCategoryListViewState()
    class LoadingMore(val recipes: PagedList<GifRecipeUI>?): RecipeCategoryListViewState()
    class LoadMoreError(val recipes: PagedList<GifRecipeUI>?): RecipeCategoryListViewState()
    class NetworkError : RecipeCategoryListViewState()
    class PagingList(val list: PagedList<GifRecipeUI>?, val finishedLoading: Boolean): RecipeCategoryListViewState()
}