package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.GifRecipeUiProviderImpl
import alexsullivan.gifrecipes.favoriting.FavoriteCache
import alexsullivan.gifrecipes.recipelist.infiniteloadinglist.RecipeDataSource
import alexsullivan.gifrecipes.utils.addTo
import alexsullivan.gifrecipes.utils.toGifRecipe
import alexsullivan.gifrecipes.viewarchitecture.BasePresenter
import android.arch.paging.DataSource.Factory
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.alexsullivan.GifRecipeRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class RecipeCategoryListPresenterImpl(searchTerm: String,
                                      private val repository: GifRecipeRepository,
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
          val factory = Factory { createAndBindDataSource(it) }
          LivePagedListBuilder(factory, 10).build().observeForever { list: PagedList<GifRecipeUI>? ->
            list?.let {
              pushValue(RecipeCategoryListViewState.PagingList(it, false))
            }
          }
        }
        .addTo(disposables)
  }

  private fun createAndBindDataSource(searchTerm: String): RecipeDataSource {
    val uiProvider = GifRecipeUiProviderImpl(repository, favoriteCache, searchTerm)
    val dataSource = RecipeDataSource(uiProvider)

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

    return dataSource
  }
}

abstract class RecipeCategoryListPresenter : BasePresenter<RecipeCategoryListViewState>() {
  companion object {
    fun create(searchTerm: String, repository: GifRecipeRepository, favoriteCache: FavoriteCache) = RecipeCategoryListPresenterImpl(searchTerm, repository, favoriteCache)
  }

  abstract fun setSearchTermSource(source: Observable<String>)
  abstract fun recipeFavoriteToggled(recipe: GifRecipeUI)
  abstract fun searchTermChanged(searchTerm: String)
}