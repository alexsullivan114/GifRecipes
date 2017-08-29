package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.favoriting.FavoriteCache
import alexsullivan.gifrecipes.utils.addTo
import alexsullivan.gifrecipes.utils.emptyLet
import alexsullivan.gifrecipes.utils.nonEmptyLet
import alexsullivan.gifrecipes.utils.toGifRecipe
import alexsullivan.gifrecipes.viewarchitecture.BasePresenter
import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import java.util.concurrent.TimeUnit

class RecipeCategoryListPresenterImpl(searchTerm: String,
                                      private val repository: GifRecipeRepository,
                                      private val favoriteCache: FavoriteCache) : RecipeCategoryListPresenter() {

    private val pageRequestSize = 10
    private var lastQueryDisposable: Disposable? = null
    private val disposables = CompositeDisposable()
    private val lastPageKeyObservable = BehaviorSubject.createDefault("")
    private val searchTermObservable = BehaviorSubject.createDefault(searchTerm)
    private val favoriteDatabaseStream = PublishSubject.create<GifRecipeUI>()
    private val reachedBottomStream = PublishSubject.create<Boolean>()

    init {
        querySearchTerm(searchTerm)
        bindSearchTermStream()
        bindSavingFavoriteDatabaseStream()
        bindFavoriteDatabaseStream()
        bindReachedBottomStream()
    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }

    override fun reduce(old: RecipeCategoryListViewState, new: RecipeCategoryListViewState): RecipeCategoryListViewState? {
        when (new) {
            // If we received a recipe list and our last value was loading more, we need to add all of the old
            // recipes to the new view state.
            is RecipeCategoryListViewState.RecipeList -> {
                if (old is RecipeCategoryListViewState.LoadingMore) {
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
                        if (value.id == new.recipe.id) {
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
        reachedBottomStream.onNext(true)
    }

    private fun loadMore() {
        lastPageKeyObservable
            .subscribeOn(Schedulers.io())
            .take(1)
            .filter { pageKey -> pageKey.isNotBlank() }
            // Check our list view state, make sure its a RecipeList. This method can be called multiple times
            // quickly depending on how the user interacts with the list (i.e. if they scroll down and up quickly
            // we could trigger the reached bottom condition a few times). If our last state is anything
            // other than a RecipeList then we want to drop it right here.
            .zipWith(stateStream.take(1), BiFunction { t1: String, t2: RecipeCategoryListViewState -> t1 to t2 })
            .filter { pair -> pair.second is RecipeCategoryListViewState.RecipeList }
            // Push out our loading view state
            .doOnNext { pair ->
                pushValue(RecipeCategoryListViewState.LoadingMore(((pair.second as RecipeCategoryListViewState.RecipeList).recipes.toList())))
            }
            .zipWith(searchTermObservable, BiFunction { t1: Pair<String, RecipeCategoryListViewState>, t2: String -> t1.first to t2})
            .flatMap { (first, second) -> repository.consumeGifRecipes(pageRequestSize, second, first) }
            .doOnNext { recipe -> lastPageKeyObservable.onNext(recipe.pageKey ?: "") }
            .flatMap(this::mapRecipeToUi)
            .toList()
            .doOnSuccess { list ->
                list.emptyLet { lastPageKeyObservable.onNext("") }
            }
            // We'll still get a list here, even if we filtered right at the beginning because of our
            // page key being blank.
            .subscribe({ list ->
                list.nonEmptyLet { pushValue(RecipeCategoryListViewState.RecipeList(list)) }
            }, {
                throw it
//                pushValue(RecipeCategoryListViewState.LoadMoreError(mutableListOf()))
            })
            .addTo(disposables)
    }

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

    private fun bindReachedBottomStream() {
        reachedBottomStream
            .subscribeOn(Schedulers.io())
            .throttleFirst(1, TimeUnit.SECONDS)
            .subscribe{ loadMore() }
            .addTo(disposables)
    }

    private fun mapRecipeToUi(recipe: GifRecipe): Observable<GifRecipeUI> {
        return favoriteCache.isRecipeFavorited(recipe.id)
            .toObservable()
            .map { GifRecipeUI(recipe.url, recipe.id, recipe.thumbnail, recipe.imageType, recipe.title, it) }
    }

    override fun searchTermChanged(searchTerm: String) {
        searchTermObservable.onNext(searchTerm)
    }

    private fun bindSearchTermStream() {
        searchTermObservable
            .subscribeOn(Schedulers.io())
            .doOnNext { lastPageKeyObservable.onNext("") }
            .distinctUntilChanged()
            .subscribe { querySearchTerm(it) }
            .addTo(disposables)
    }

    private fun querySearchTerm(searchTerm: String) {
        lastQueryDisposable?.dispose()
        lastQueryDisposable = lastPageKeyObservable
            .take(1)
            .flatMap { pageKey -> repository.consumeGifRecipes(pageRequestSize, searchTerm, pageKey) }
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { pushValue(RecipeCategoryListViewState.Loading()) }
            .doOnNext { lastPageKeyObservable.onNext(it.pageKey ?: "") }
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

    override fun recipeFavoriteToggled(recipe: GifRecipeUI) {
        favoriteDatabaseStream.onNext(recipe)
    }
}

abstract class RecipeCategoryListPresenter : BasePresenter<RecipeCategoryListViewState>() {
    companion object {
        fun create(searchTerm: String, repository: GifRecipeRepository, favoriteCache: FavoriteCache) = RecipeCategoryListPresenterImpl(searchTerm, repository, favoriteCache)
    }

    abstract fun reachedBottom()
    abstract fun setSearchTermSource(source: Observable<String>)
    abstract fun recipeFavoriteToggled(recipe: GifRecipeUI)
    abstract fun searchTermChanged(searchTerm: String)
}