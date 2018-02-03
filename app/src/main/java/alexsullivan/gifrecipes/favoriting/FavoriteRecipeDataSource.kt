package alexsullivan.gifrecipes.favoriting

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.database.toGifRecipe
import alexsullivan.gifrecipes.utils.datasourceutils.DataSourceErrorProvider
import alexsullivan.gifrecipes.utils.datasourceutils.GifDataSourceFactory
import alexsullivan.gifrecipes.utils.toGifRecipeUI
import android.arch.paging.DataSource
import android.arch.paging.PositionalDataSource
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

class FavoriteRecipeDataSource(private val favoriteGifRecipeRepository: FavoriteGifRecipeRepository): PositionalDataSource<GifRecipeUI>(), DataSourceErrorProvider {

  private val initialLoadingSubject = BehaviorSubject.create<Boolean>()
  private val furtherLoadingSubject = BehaviorSubject.create<Boolean>()

  private val initialLoadingErrorSubject = BehaviorSubject.create<Throwable>()
  private val furtherLoadingErrorSubject = BehaviorSubject.create<Throwable>()

  override val initialLoadingFlowable = initialLoadingSubject.toFlowable(BackpressureStrategy.BUFFER)
  override val futherLoadingFlowable = furtherLoadingSubject.toFlowable(BackpressureStrategy.BUFFER)

  override val initialLoadingErrorFlowable = initialLoadingErrorSubject.toFlowable(BackpressureStrategy.BUFFER)
  override val futherLoadingErrorFlowable = furtherLoadingErrorSubject.toFlowable(BackpressureStrategy.BUFFER)

  override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<GifRecipeUI>) {
    favoriteGifRecipeRepository
        .gifRecipeDao
        .findFavorites(params.startPosition, params.loadSize)
        .flatMap { Flowable.just(it.map { it.toGifRecipe().toGifRecipeUI(true) }) }
        .firstElement()
        .doOnSubscribe { furtherLoadingSubject.onNext(true) }
        .doFinally { furtherLoadingSubject.onNext(false) }
        .subscribe({
          callback.onResult(it)
        }, {
          furtherLoadingErrorSubject.onNext(it)
        })
  }

  override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<GifRecipeUI>) {
    favoriteGifRecipeRepository
        .gifRecipeDao
        .findFavorites(params.requestedStartPosition, params.requestedLoadSize)
        .flatMap { Flowable.just(it.map { it.toGifRecipe().toGifRecipeUI(true) }) }
        .firstElement()
        .doOnSubscribe { initialLoadingSubject.onNext(true) }
        .doFinally { initialLoadingSubject.onNext(false) }
        .subscribe ({ recipes ->
          callback.onResult(recipes, 0)
        }, {
          initialLoadingErrorSubject.onNext(it)
        })
  }

  companion object {
    fun factory(favoriteGifRecipeRepository: FavoriteGifRecipeRepository): GifDataSourceFactory {
      return object: GifDataSourceFactory {
        override fun create(searchTerm: String, favoriteCache: FavoriteCache): Pair<DataSource<*, GifRecipeUI>, DataSourceErrorProvider> {
          val dataSource = FavoriteRecipeDataSource(favoriteGifRecipeRepository)
          return dataSource to dataSource
        }
      }
    }
  }
}