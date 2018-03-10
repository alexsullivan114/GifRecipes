package alexsullivan.gifrecipes.recipelist.infiniteloadinglist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.GifRecipeUiProvider
import alexsullivan.gifrecipes.GifRecipeUiProviderImpl
import alexsullivan.gifrecipes.favoriting.FavoriteCache
import alexsullivan.gifrecipes.utils.datasourceutils.DataSourceErrorProvider
import alexsullivan.gifrecipes.utils.datasourceutils.GifDataSourceFactory
import android.arch.paging.DataSource
import android.arch.paging.PageKeyedDataSource
import com.alexsullivan.GifRecipeProvider
import com.alexsullivan.GifRecipeRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class RecipeDataSource(private val gifRecipeUiProvider: GifRecipeUiProvider) : PageKeyedDataSource<Observable<GifRecipeProvider.Response>, GifRecipeUI>(), DataSourceErrorProvider {

  private val initialLoadingSubject = BehaviorSubject.create<Boolean>()
  private val furtherLoadingSubject = BehaviorSubject.create<Boolean>()

  private val initialLoadingErrorSubject = BehaviorSubject.create<Throwable>()
  private val furtherLoadingErrorSubject = BehaviorSubject.create<Throwable>()

  override val initialLoadingFlowable = initialLoadingSubject.toFlowable(BackpressureStrategy.BUFFER)
  override val futherLoadingFlowable = furtherLoadingSubject.toFlowable(BackpressureStrategy.BUFFER)

  override val initialLoadingErrorFlowable = initialLoadingErrorSubject.toFlowable(BackpressureStrategy.BUFFER)
  override val futherLoadingErrorFlowable = furtherLoadingErrorSubject.toFlowable(BackpressureStrategy.BUFFER)

  override fun loadAfter(params: LoadParams<Observable<GifRecipeProvider.Response>>, callback: LoadCallback<Observable<GifRecipeProvider.Response>, GifRecipeUI>) {
    gifRecipeUiProvider.fetchRecipes(params.key)
        .doOnSubscribe { furtherLoadingSubject.onNext(true) }
        .doFinally { furtherLoadingSubject.onNext(false) }
        .subscribe({ data ->
          callback.onResult(data.second, data.first)
        }, {
          furtherLoadingErrorSubject.onNext(it)
        })
  }

  override fun loadInitial(params: LoadInitialParams<Observable<GifRecipeProvider.Response>>, callback: LoadInitialCallback<Observable<GifRecipeProvider.Response>, GifRecipeUI>) {
    gifRecipeUiProvider.fetchRecipes(params.requestedLoadSize)
        .doOnSubscribe { initialLoadingSubject.onNext(true) }
        .doFinally { initialLoadingSubject.onNext(false) }
        .subscribe({ recipes ->
          callback.onResult(recipes.second, null, recipes.first)
        }, {
          initialLoadingErrorSubject.onNext(it)
        })
  }

  override fun loadBefore(params: LoadParams<Observable<GifRecipeProvider.Response>>, callback: LoadCallback<Observable<GifRecipeProvider.Response>, GifRecipeUI>) {
    //no-op, we always keep the whole gif recipe list in memory.
  }

  companion object {
    fun factory(gifRecipeRepository: GifRecipeRepository): GifDataSourceFactory {
      return object : GifDataSourceFactory {
        override fun create(searchTerm: String, favoriteCache: FavoriteCache): Pair<DataSource<*, GifRecipeUI>, DataSourceErrorProvider> {
          val dataSource = RecipeDataSource(GifRecipeUiProviderImpl(gifRecipeRepository, favoriteCache, searchTerm))
          return dataSource to dataSource
        }
      }
    }
  }
}