package alexsullivan.gifrecipes.recipelist;

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.database.RecipeDatabase
import alexsullivan.gifrecipes.database.toFavorite
import alexsullivan.gifrecipes.toGifRecipe
import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import java.util.concurrent.TimeUnit

class RecipeCategoryListPresenterImpl(searchTerm: String,
                                      val repository: GifRecipeRepository,
                                      val recipeDatabase: RecipeDatabase) : RecipeCategoryListPresenter {

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

    override val stateStream: BehaviorSubject<RecipeCategoryListViewState> by lazy {
        BehaviorSubject.create<RecipeCategoryListViewState>()
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

    override fun reachedBottom() {
        // If our state stream hasn't returned anything (shouldn't be possible) then bail.
        if (!stateStream.hasValue()) return
        val lastValue = stateStream.value
        if (lastValue is RecipeCategoryListViewState.RecipeList) {
            // If our last page key is blank, then we must've run out of items.
            if (lastPageKey != "") {
                // Send the message that we're loading new items.
                stateStream.onNext(RecipeCategoryListViewState.LoadingMore(lastValue.recipes))
                disposables.add(repository.consumeGifRecipes(pageRequestSize, searchTerm, lastPageKey)
                        .doOnNext { lastPageKey = it.pageKey ?: "" }
                        .map(this::mapRecipeToUi)
                        .toList()
                        .doOnSuccess {
                            if (it.size == 0) {
                                // Zero new items so we won't hit doOnNext, so we need to remember that
                                // we're out.
                                lastPageKey = ""
                            }
                        }
                        .map {
                            it.addAll(0, lastValue.recipes)
                            it
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            stateStream.onNext(RecipeCategoryListViewState.RecipeList(it))
                        }, {
                            stateStream.onNext(RecipeCategoryListViewState.LoadMoreError(lastValue.recipes))
                        }))
            }
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
                recipeDatabase.gifRecipeDao().insertFavoriteRecipe(recipe.toGifRecipe().toFavorite())
            } else {
                recipeDatabase.gifRecipeDao().deleteFavoriteRecipe(recipe.toGifRecipe().toFavorite())
            }
        }

        favoriteDatabaseStream
                .observeOn(Schedulers.io())
                .subscribe {
                    saveFavorite(it)
                }
    }

    private fun bindFavoriteDatabaseStream() {
//        recipeDatabase.gifRecipeDao().findFavorites()
    }

    private fun querySearchTerm() {
        lastQueryDisposable?.dispose()
        lastQueryDisposable = repository.consumeGifRecipes(pageRequestSize, searchTerm, lastPageKey)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { stateStream.onNext(RecipeCategoryListViewState.Loading()) }
                .doOnNext { lastPageKey = it.pageKey ?: lastPageKey }
                .map(this::mapRecipeToUi)
                .toList()
                .subscribe({ result: List<GifRecipeUI> ->
                    stateStream.onNext(RecipeCategoryListViewState.RecipeList(result))
                }, {
                    if (it is IOException) {
                        stateStream.onNext(RecipeCategoryListViewState.NetworkError())
                    }
                })
        lastQueryDisposable?.let {
            disposables.add(it)
        }
    }

    private fun mapRecipeToUi(recipe: GifRecipe): GifRecipeUI {
        val favorited = recipeDatabase.gifRecipeDao().isRecipeFavorited(recipe.id)
        return GifRecipeUI(recipe.url, recipe.id, recipe.thumbnail, recipe.imageType, recipe.title, favorited)
    }

    override fun recipeFavoriteToggled(recipe: GifRecipeUI) {
        favoriteDatabaseStream.onNext(recipe)
    }
}

interface RecipeCategoryListPresenter : Presenter<RecipeCategoryListViewState> {
    companion object {
        fun create(searchTerm: String, repository: GifRecipeRepository, database: RecipeDatabase): RecipeCategoryListPresenter {
            return RecipeCategoryListPresenterImpl(searchTerm, repository, database)
        }
    }

    fun reachedBottom()
    fun setSearchTermSource(source: Observable<String>)
    fun recipeFavoriteToggled(recipe: GifRecipeUI)
    var searchTerm: String
}

sealed class RecipeCategoryListViewState : ViewState {
    class Loading : RecipeCategoryListViewState()
    class LoadingMore(val recipes: List<GifRecipeUI>): RecipeCategoryListViewState()
    class RecipeList(val recipes: List<GifRecipeUI>): RecipeCategoryListViewState()
    class LoadMoreError(val recipes: List<GifRecipeUI>): RecipeCategoryListViewState()
    class NetworkError : RecipeCategoryListViewState()
}