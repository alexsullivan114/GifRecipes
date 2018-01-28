package alexsullivan.gifrecipes.favoriting

import alexsullivan.gifrecipes.database.GifRecipeDao
import alexsullivan.gifrecipes.database.toFavorite
import com.alexsullivan.GifRecipe
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class RoomFavoriteCache private constructor(val gifRecipeDao: GifRecipeDao) : FavoriteCache {

  private val recipeMap = HashMap<String, Boolean>()
  private val favoriteStream = PublishSubject.create<Pair<GifRecipe, Boolean>>()
  private val initialFetchDisposable: Disposable
  private val initialFetchCompletedSubject = BehaviorSubject.create<Boolean>()

  init {
    initialFetchDisposable = Observable.fromCallable { gifRecipeDao.findFavorites() }
        .subscribeOn(Schedulers.io())
        .flatMap { Observable.fromIterable(it) }
        .doFinally { initialFetchCompletedSubject.onNext(true) }
        .subscribe {
          recipeMap.put(it.id, true)
        }

    bindSavingFavoriteDatabaseStream()
  }

  override fun isRecipeFavorited(id: String): Observable<Boolean> {
    return initialFetchCompletedSubject
        .firstOrError()
        .map {
          if (recipeMap.containsKey(id)) {
            recipeMap.getValue(id)
          } else {
            false
          }
        }
        .toObservable()
        .concatWith(favoriteStream.filter { it.first.id == id }.map { it.second })
  }

  override fun insertFavoriteRecipe(gifRecipe: GifRecipe): Completable {
    return initialFetchCompletedSubject
        .firstOrError()
        .flatMapCompletable {
          recipeMap.put(gifRecipe.id, true)
          favoriteStream.onNext(Pair(gifRecipe, true))
          Completable.complete()
        }
  }

  override fun deleteFavoriteRecipe(gifRecipe: GifRecipe): Completable {
    return initialFetchCompletedSubject
        .firstOrError()
        .flatMapCompletable {
          recipeMap.put(gifRecipe.id, false)
          favoriteStream.onNext(Pair(gifRecipe, false))
          Completable.complete()
        }
  }

  override fun favoriteStateChangedFlowable() = favoriteStream.toFlowable(BackpressureStrategy.BUFFER)

  private fun bindSavingFavoriteDatabaseStream() {
    val saveFavorite = fun(recipe: GifRecipe, favorite: Boolean) {
      if (favorite) {
        gifRecipeDao.insertFavoriteRecipe(recipe.toFavorite())
      } else {
        gifRecipeDao.deleteFavoriteRecipe(recipe.toFavorite())
      }
    }

    favoriteStream
        .observeOn(Schedulers.io())
        .subscribe {
          saveFavorite(it.first, it.second)
        }
  }

  companion object {

    private var cache: RoomFavoriteCache? = null;

    fun getInstance(gifRecipeDao: GifRecipeDao): RoomFavoriteCache {
      var cacheCopy = cache
      if (cacheCopy != null) {
        return cacheCopy
      } else {
        cache = RoomFavoriteCache(gifRecipeDao)
        cacheCopy = cache
        if (cacheCopy != null) {
          return cacheCopy;
        }
      }

      throw RuntimeException("Reached unreachable null failures in room favorite cache initilizaiton")
    }
  }
}