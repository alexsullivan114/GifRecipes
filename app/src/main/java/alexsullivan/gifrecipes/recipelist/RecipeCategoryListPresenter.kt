package alexsullivan.gifrecipes.recipelist;

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.database.FavoriteCache
import alexsullivan.gifrecipes.toGifRecipe
import alexsullivan.gifrecipes.viewarchitecture.BasePresenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import java.util.concurrent.TimeUnit

class RecipeCategoryListPresenterImpl(searchTerm: String,
                                      val repository: GifRecipeRepository,
                                      val favoriteCache: FavoriteCache) : RecipeCategoryListPresenter() {

    val disposables = CompositeDisposable()
    val pageRequestSize = 10
    var lastPageKey = ""
    var lastQueryDisposable: Disposable? = null
    val favoriteDatabaseStream: PublishSubject<GifRecipeUI> = PublishSubject.create()

    override var searchTerm = searchTerm
        set(value) {
            field = value
            lastPageKey = ""
            querySearchTerm()
        }

    init {
        querySearchTerm()
        bindSavingFavoriteDatabaseStream()
        bindFavoriteDatabaseStream()
    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }

    override fun reduce(old: RecipeCategoryListViewState, new: RecipeCategoryListViewState): RecipeCategoryListViewState? {
        when (new) {
            // If we received a Loading more view state, we need to add all of the old recipes we had.
            is RecipeCategoryListViewState.LoadingMore -> {
                if (old is RecipeCategoryListViewState.RecipeList) {
                    new.recipes.addAll(old.recipes)
                }
                // If we received two loading more view states, we must've re-triggered the bottom endless scrolling
                // check. We should just wait for the first loading more to finish in that case.
                if (old is RecipeCategoryListViewState.LoadingMore) {
                    return null
                }

                return new
            }
            // If we received a recipe list and our last value was loading more, we need to add all of the old
            // recipes to the new view state.
            is RecipeCategoryListViewState.RecipeList -> {
                if (old is RecipeCategoryListViewState.LoadingMore) {
                    new.recipes.addAll(0, old.recipes)
                }

                if (old is RecipeCategoryListViewState.RecipeList) {
                    new.recipes.addAll(0, old.recipes)
                }
                return new
            }
            // If we received a loading more error, we need to add all of the old recipes in the list
            // so it can properly display everything.
            is RecipeCategoryListViewState.LoadMoreError -> {
                if (old is RecipeCategoryListViewState.LoadingMore) {
                    new.recipes.addAll(old.recipes)
                }

                if (old is RecipeCategoryListViewState.RecipeList) {
                    new.recipes.addAll(old.recipes)
                }
                return new
            }
            is RecipeCategoryListViewState.Favorited -> {
                if (old is RecipeCategoryListViewState.LoadingMore || old is RecipeCategoryListViewState.RecipeList) {
                    val recipes = mutableListOf<GifRecipeUI>()
                    if (old is RecipeCategoryListViewState.LoadingMore) {
                        recipes.addAll(old.recipes)
                    } else if (old is RecipeCategoryListViewState.RecipeList) {
                        recipes.addAll(old.recipes)
                    }

                    for ((index, value) in recipes.withIndex()) {
                        if (value.id == new.recipeId) {
                            recipes[index] = value.copy(favorite = new.isFavorite)
                        }
                    }

                    if (old is RecipeCategoryListViewState.LoadingMore) {
                        return RecipeCategoryListViewState.LoadingMore(recipes)
                    } else if (old is RecipeCategoryListViewState.RecipeList) {
                        return RecipeCategoryListViewState.RecipeList(recipes)
                    }

                    // Can't ever get here...
                    return null

                } else {
                    // Shouldn't ever happen...
                    return null
                }
            }
            else -> {
                return new
            }
        }
    }

    override fun reachedBottom() {
        // If our last page key is blank, then we must've run out of items.
        if (lastPageKey != "") {
            // Send the message that we're loading new items.
            pushValue(RecipeCategoryListViewState.LoadingMore(mutableListOf()))
            disposables.add(repository.consumeGifRecipes(pageRequestSize, searchTerm, lastPageKey)
                    .doOnNext { lastPageKey = it.pageKey ?: "" }
                    .flatMap(this::mapRecipeToUi)
                    .toList()
                    .doOnSuccess {
                        if (it.size == 0) {
                            // Zero new items so we won't hit doOnNext, so we need to remember that
                            // we're out.
                            lastPageKey = ""
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        pushValue(RecipeCategoryListViewState.RecipeList(it))
                    }, {
                        pushValue(RecipeCategoryListViewState.LoadMoreError(mutableListOf()))
                    }))
        }
    }

    override fun setSearchTermSource(source: Observable<String>) {
        disposables.add(source
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe { searchTerm = it })
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
    }

    private fun bindFavoriteDatabaseStream() {
        disposables.add(favoriteCache.favoriteStateChangedFlowable()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    pushValue(RecipeCategoryListViewState.Favorited(it.second, it.first.id))
                })
    }

    private fun querySearchTerm() {
        lastQueryDisposable?.dispose()
        lastQueryDisposable = repository.consumeGifRecipes(pageRequestSize, searchTerm, lastPageKey)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { pushValue(RecipeCategoryListViewState.Loading()) }
                .doOnNext { lastPageKey = it.pageKey ?: lastPageKey }
                .flatMap(this::mapRecipeToUi)
                .toList()
                .subscribe({ result: MutableList<GifRecipeUI> ->
                    pushValue(RecipeCategoryListViewState.RecipeList(result))
                }, {
                    if (it is IOException) {
                        pushValue(RecipeCategoryListViewState.NetworkError())
                    }
                })
        lastQueryDisposable?.let {
            disposables.add(it)
        }
    }

    private fun mapRecipeToUi(recipe: GifRecipe): Observable<GifRecipeUI> {
        return favoriteCache.isRecipeFavorited(recipe.id)
                .toObservable()
                .map { GifRecipeUI(recipe.url, recipe.id, recipe.thumbnail, recipe.imageType, recipe.title, it) }
    }

    override fun recipeFavoriteToggled(recipe: GifRecipeUI) {
        favoriteDatabaseStream.onNext(recipe)
    }
}

abstract class RecipeCategoryListPresenter : BasePresenter<RecipeCategoryListViewState>() {
    companion object {
        fun create(searchTerm: String, repository: GifRecipeRepository, favoriteCache: FavoriteCache): RecipeCategoryListPresenter {
            return RecipeCategoryListPresenterImpl(searchTerm, repository, favoriteCache)
        }
    }

    abstract fun reachedBottom()
    abstract fun setSearchTermSource(source: Observable<String>)
    abstract fun recipeFavoriteToggled(recipe: GifRecipeUI)
    abstract var searchTerm: String
}

sealed class RecipeCategoryListViewState : ViewState {
    class Loading : RecipeCategoryListViewState()
    class LoadingMore(val recipes: MutableList<GifRecipeUI>): RecipeCategoryListViewState()
    class RecipeList(val recipes: MutableList<GifRecipeUI>): RecipeCategoryListViewState()
    class LoadMoreError(val recipes: MutableList<GifRecipeUI>): RecipeCategoryListViewState()
    class NetworkError : RecipeCategoryListViewState()
    class Favorited(val isFavorite: Boolean, val recipeId: String): RecipeCategoryListViewState()
}