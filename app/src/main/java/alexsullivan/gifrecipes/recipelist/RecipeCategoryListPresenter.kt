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
    bindFavoriteDatabaseStream()
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

  private fun bindFavoriteDatabaseStream() {
    favoriteCache.favoriteStateChangedFlowable()
        .subscribeOn(Schedulers.io())
        .subscribe {
          pushValue(RecipeCategoryListViewState.Favorited(it.second, it.first))
        }
        .addTo(disposables)
  }

  override fun searchTermChanged(searchTerm: String) {
    searchTermObservable.onNext(searchTerm)
  }

  private fun bindSearchTermStream() {
    searchTermObservable
        .subscribeOn(Schedulers.io())
        .distinctUntilChanged()
        .subscribe {
          val uiProvider = GifRecipeUiProviderImpl(repository, favoriteCache, it)
          val factory = Factory { RecipeDataSource(uiProvider) }
          LivePagedListBuilder(factory, 10).build().observeForever { list: PagedList<GifRecipeUI>? ->
            list?.let {
              pushValue(RecipeCategoryListViewState.PagingList(it))
            }
          }
        }
        .addTo(disposables)
  }

  override fun recipeFavoriteToggled(recipe: GifRecipeUI) {
    favoriteDatabaseStream.onNext(recipe)
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