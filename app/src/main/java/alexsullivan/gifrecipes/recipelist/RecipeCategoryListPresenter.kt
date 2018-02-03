package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.favoriting.FavoriteCache
import alexsullivan.gifrecipes.utils.addTo
import alexsullivan.gifrecipes.utils.datasourceutils.DataSourceErrorProvider
import alexsullivan.gifrecipes.utils.datasourceutils.GifDataSourceFactory
import alexsullivan.gifrecipes.utils.toGifRecipe
import alexsullivan.gifrecipes.viewarchitecture.BasePresenter
import android.arch.paging.DataSource
import android.arch.paging.DataSource.Factory
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class RecipeCategoryListPresenterImpl(searchTerm: String,
                                      private val gifDataSourceFactory: GifDataSourceFactory,
                                      private val favoriteCache: FavoriteCache) : RecipeCategoryListPresenter() {

  private val disposables = CompositeDisposable()
  private val searchTermObservable = BehaviorSubject.createDefault(searchTerm)
  private val favoriteDatabaseStream = PublishSubject.create<GifRecipeUI>()

  init {
    bindSearchTermStream()
    bindSavingFavoriteDatabaseStream()
  }

  override fun destroy() {
    super.destroy()
    disposables.clear()
  }

  override fun reduce(old: RecipeCategoryListViewState, new: RecipeCategoryListViewState): RecipeCategoryListViewState? =
      old.reduce(new)

  override fun setSearchTermSource(source: Observable<String>) {
    source.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .debounce(500, TimeUnit.MILLISECONDS)
        .subscribe {
          searchTermObservable.onNext(it)
        }
        .addTo(disposables)
  }

  override fun searchTermChanged(searchTerm: String) {
    searchTermObservable.onNext(searchTerm)
  }

  override fun recipeFavoriteToggled(recipe: GifRecipeUI) {
    favoriteDatabaseStream.onNext(recipe)
  }

  private fun bindSavingFavoriteDatabaseStream() {
    val saveFavorite = fun(recipe: GifRecipeUI) {
      if (recipe.favorite) {
        favoriteCache.insertFavoriteRecipe(recipe.toGifRecipe()).subscribeOn(Schedulers.io()).subscribe()
      } else {
        favoriteCache.deleteFavoriteRecipe(recipe.toGifRecipe()).subscribeOn(Schedulers.io()).subscribe()
      }
    }

    favoriteDatabaseStream
        .observeOn(Schedulers.io())
        .subscribe {
          saveFavorite(it)
        }
        .addTo(disposables)
  }

  private fun bindSearchTermStream() {
    searchTermObservable
        .subscribeOn(Schedulers.io())
        .distinctUntilChanged()
        .subscribe {
          val factory = Factory<Any, GifRecipeUI> { createAndBindDataSource(it) as DataSource<Any, GifRecipeUI>? }
          val config = PagedList.Config.Builder().setEnablePlaceholders(false).setPageSize(10).build()
          LivePagedListBuilder(factory, config).build().observeForever { list: PagedList<GifRecipeUI>? ->
            list?.let {
              pushValue(RecipeCategoryListViewState.PagingList(it, false))
            }
          }
        }
        .addTo(disposables)
  }

  private fun createAndBindDataSource(searchTerm: String): DataSource<*, GifRecipeUI> {
    val dataSource = gifDataSourceFactory.create(searchTerm, favoriteCache)
    bindDataSourceEvents(dataSource.second)
    return dataSource.first
  }

  private fun bindDataSourceEvents(dataSource: DataSourceErrorProvider) {
    dataSource.initialLoadingFlowable
        .subscribeOn(Schedulers.io())
        .subscribe {
          if (it) {
            pushValue(RecipeCategoryListViewState.Loading())
          } else {
            pushValue(RecipeCategoryListViewState.PagingList(null, true))
          }
        }
        .addTo(disposables)

    dataSource.futherLoadingFlowable
        .subscribeOn(Schedulers.io())
        .subscribe {
          if (it) {
            pushValue(RecipeCategoryListViewState.LoadingMore(null))
          } else {
            pushValue(RecipeCategoryListViewState.PagingList(null, true))
          }
        }
        .addTo(disposables)

    dataSource.initialLoadingErrorFlowable
        .subscribeOn(Schedulers.io())
        .subscribe { pushValue(RecipeCategoryListViewState.NetworkError()) }
        .addTo(disposables)

    dataSource.futherLoadingErrorFlowable
        .subscribeOn(Schedulers.io())
        .subscribe { pushValue(RecipeCategoryListViewState.LoadMoreError(null)) }
        .addTo(disposables)
  }
}

abstract class RecipeCategoryListPresenter : BasePresenter<RecipeCategoryListViewState>() {
  companion object {
    fun create(searchTerm: String, gifDataSourceFactory: GifDataSourceFactory, favoriteCache: FavoriteCache) = RecipeCategoryListPresenterImpl(searchTerm, gifDataSourceFactory, favoriteCache)
  }

  abstract fun setSearchTermSource(source: Observable<String>)
  abstract fun recipeFavoriteToggled(recipe: GifRecipeUI)
  abstract fun searchTermChanged(searchTerm: String)
}