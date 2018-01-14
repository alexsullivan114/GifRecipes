package alexsullivan.gifrecipes.recipelist.infiniteloadinglist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.GifRecipeUiProvider
import android.arch.paging.PageKeyedDataSource

class RecipeDataSource(private val gifRecipeUiProvider: GifRecipeUiProvider): PageKeyedDataSource<String, GifRecipeUI>() {
  override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, GifRecipeUI>) {
    gifRecipeUiProvider.fetchRecipes(params.requestedLoadSize, params.key)
        .subscribe { recipes ->
          callback.onResult(recipes.second, recipes.first)
        }
  }

  override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, GifRecipeUI>) {
    gifRecipeUiProvider.fetchRecipes(params.requestedLoadSize,"")
        .subscribe { recipes ->
          callback.onResult(recipes.second, null, recipes.first)
        }
  }

  override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, GifRecipeUI>) {
    //no-op, we always keep the whole gif recipe list in memory.
  }
}