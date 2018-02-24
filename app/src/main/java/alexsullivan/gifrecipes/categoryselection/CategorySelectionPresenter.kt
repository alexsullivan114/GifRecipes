package alexsullivan.gifrecipes.categoryselection;

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.categoryselection.CategorySelectionViewState.*
import alexsullivan.gifrecipes.utils.addTo
import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import com.alexsullivan.GifRecipeRepository
import com.alexsullivan.logging.Logger
import com.alexsullivan.utils.TAG
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.io.IOException


class CategorySelectionPresenterImpl(repository: GifRecipeRepository,
                                     backgroundScheduler: Scheduler,
                                     private val logger: Logger) : CategorySelectionPresenter() {

  val disposables = CompositeDisposable()

  override val stateStream = BehaviorSubject.create<CategorySelectionViewState>()

  init {
    var startTime = 0L
    repository.consumeGifRecipes(15)
        .subscribeOn(backgroundScheduler)
        // First push out our loading screen...
        .doOnSubscribe {
          stateStream.onNext(FetchingGifs())
          startTime = System.currentTimeMillis()
        }
        .flatMap { Observable.fromIterable(it.recipes) }
        .map { it.copy(url = it.url, imageType = it.imageType) }
        .map { GifRecipeUI(it.url, it.id, it.thumbnail, it.imageType, it.title) }
        .toList()
        .subscribe({ list: MutableList<GifRecipeUI> ->
          val endTime = System.currentTimeMillis() - startTime
          logger.d(TAG, "Total processing time took $endTime milliseconds")
          stateStream.onNext(GifList(list))
        }, {
          if (it is IOException) {
            stateStream.onNext(NetworkError())
          } else {
            throw it
          }
        })
        .addTo(disposables)

  }

  override fun destroy() {
    super.destroy()
    disposables.dispose()
  }
}

abstract class CategorySelectionPresenter : Presenter<CategorySelectionViewState> {
  companion object {
    fun create(gifRecipeRepository: GifRecipeRepository, backgroundScheduler: Scheduler, logger: Logger) =
        CategorySelectionPresenterImpl(gifRecipeRepository, backgroundScheduler, logger)
  }
}

sealed class CategorySelectionViewState : ViewState {
  class FetchingGifs : CategorySelectionViewState()
  data class GifList(val gifRecipes: List<GifRecipeUI>) : CategorySelectionViewState()
  class NetworkError : CategorySelectionViewState()
}