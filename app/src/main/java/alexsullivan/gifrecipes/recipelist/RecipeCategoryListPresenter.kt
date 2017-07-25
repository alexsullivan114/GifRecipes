package alexsullivan.gifrecipes.recipelist;

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import com.alexsullivan.GifRecipeRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class RecipeCategoryListPresenterImpl(val searchTerm: String,
                                      val repository: GifRecipeRepository) : RecipeCategoryListPresenter {

    val disposables = CompositeDisposable()
    val pageRequestSize = 5
    var lastPageKey = ""

    override val stateStream: BehaviorSubject<RecipeCategoryListViewState> by lazy {
        BehaviorSubject.create<RecipeCategoryListViewState>()
    }

    init {
        disposables.add(repository.consumeGifRecipes(pageRequestSize, searchTerm, lastPageKey)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { stateStream.onNext(RecipeCategoryListViewState.Loading()) }
                .doOnNext { lastPageKey = it.pageKey ?: lastPageKey }
                .map { GifRecipeUI(it.url, it.thumbnail, it.imageType, it.title) }
                .toList()
                .subscribe({ result: List<GifRecipeUI> ->
                    stateStream.onNext(RecipeCategoryListViewState.RecipeList(result))
                }))

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
                        .map { GifRecipeUI(it.url, it.thumbnail, it.imageType, it.title) }
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
}

interface RecipeCategoryListPresenter : Presenter<RecipeCategoryListViewState> {
    companion object {
        fun create(searchTerm: String, repository: GifRecipeRepository): RecipeCategoryListPresenter {
            return RecipeCategoryListPresenterImpl(searchTerm, repository)
        }
    }

    fun reachedBottom()
}

sealed class RecipeCategoryListViewState : ViewState {
    class Loading : RecipeCategoryListViewState()
    class LoadingMore(val recipes: List<GifRecipeUI>): RecipeCategoryListViewState()
    class RecipeList(val recipes: List<GifRecipeUI>): RecipeCategoryListViewState()
    class LoadMoreError(val recipes: List<GifRecipeUI>): RecipeCategoryListViewState()
}