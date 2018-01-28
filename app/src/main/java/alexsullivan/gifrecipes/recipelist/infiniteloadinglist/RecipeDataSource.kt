package alexsullivan.gifrecipes.recipelist.infiniteloadinglist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.GifRecipeUiProvider
import android.arch.paging.PageKeyedDataSource
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject

class RecipeDataSource(private val gifRecipeUiProvider: GifRecipeUiProvider) : PageKeyedDataSource<String, GifRecipeUI>() {

  private val initialLoadingSubject = PublishSubject.create<Boolean>()
  private val furtherLoadingSubject = PublishSubject.create<Boolean>()

  val initialLoadingFlowable = initialLoadingSubject.toFlowable(BackpressureStrategy.BUFFER)
  val futherLoadingFlowable = furtherLoadingSubject.toFlowable(BackpressureStrategy.BUFFER)

  override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, GifRecipeUI>) {
    gifRecipeUiProvider.fetchRecipes(params.requestedLoadSize, params.key)
        .doOnSubscribe { furtherLoadingSubject.onNext(true) }
        .doFinally { furtherLoadingSubject.onNext(false) }
        .subscribe { recipes ->
          callback.onResult(recipes.second, recipes.first)
        }
  }

  override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, GifRecipeUI>) {
    gifRecipeUiProvider.fetchRecipes(params.requestedLoadSize, "")
        .doOnSubscribe { initialLoadingSubject.onNext(true) }
        .doFinally { initialLoadingSubject.onNext(false) }
        .subscribe { recipes ->
          callback.onResult(recipes.second, null, recipes.first)
        }
  }

  override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, GifRecipeUI>) {
    //no-op, we always keep the whole gif recipe list in memory.
  }
}