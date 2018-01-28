package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.favoriting.FavoriteCache
import alexsullivan.gifrecipes.favoriting.FavoriteGifRecipeRepository
import alexsullivan.gifrecipes.utils.addTo
import alexsullivan.gifrecipes.utils.toGifRecipe
import alexsullivan.gifrecipes.utils.toGifRecipeUI
import com.alexsullivan.GifRecipe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Alexs on 8/21/2017.
 */
class FavoriteRecipeListPresenter(private val repository: FavoriteGifRecipeRepository, private val favoriteCache: FavoriteCache) : RecipeCategoryListPresenter() {

  private val disposables = CompositeDisposable()
  private val favoriteDatabaseStream = PublishSubject.create<GifRecipeUI>()

  init {
    bindFavoriteQueryStream()
    bindSavingFavoriteDatabaseStream()
    bindFavoriteDatabaseStream()
  }

  override fun setSearchTermSource(source: Observable<String>) {
    // no-op. This presenter should not be used for a searchable interface
  }

  override fun recipeFavoriteToggled(recipe: GifRecipeUI) {
    favoriteDatabaseStream.onNext(recipe)
  }

  override fun searchTermChanged(searchTerm: String) {
    // no-op. This presenter should not have its search term changed, and its assumed it will always
    // be used for favorite recipes.
  }

  override fun reduce(old: RecipeCategoryListViewState, new: RecipeCategoryListViewState): RecipeCategoryListViewState? {
    when (new) {
      is RecipeCategoryListViewState.Favorited -> {
        if (old is RecipeCategoryListViewState.RecipeList) {
          val recipes = mutableListOf<GifRecipeUI>()
          recipes.addAll(old.recipes)
          val recipeContained = recipes.map { it.id }.contains(new.recipe.id)
          // If this recipe isn't contained in our list and we just favorited it,
          // we need to add it.
          if (!recipeContained && new.isFavorite) {
            recipes.add(new.recipe.toGifRecipeUI(new.isFavorite))
          } else if (recipeContained) {
            // Otherwise if this recipe is contained in our list we need to update its state.
            for ((index, value) in recipes.withIndex()) {
              if (value.id == new.recipe.id) {
                recipes[index] = value.copy(favorite = new.isFavorite)
              }
            }
            // Don't remove the recipe if its bee un-favorited. That would make for a
            // jarring experience.
          }

          return RecipeCategoryListViewState.RecipeList(recipes)
        }
      }
    }

    return super.reduce(old, new)
  }

  private fun bindFavoriteQueryStream() {
    repository.consumeGifRecipes(0)
        .flatMap(this::mapRecipeToUi)
        .toList()
        .subscribeOn(Schedulers.io())
        .subscribe({ pushValue(RecipeCategoryListViewState.RecipeList(it)) }, {})
        .addTo(disposables)
  }

  private fun mapRecipeToUi(recipe: GifRecipe): Observable<GifRecipeUI> {
    return favoriteCache.isRecipeFavorited(recipe.id)
        .firstOrError()
        .toObservable()
        .map { GifRecipeUI(recipe.url, recipe.id, recipe.thumbnail, recipe.imageType, recipe.title, it) }
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
        }.addTo(disposables)
  }

  private fun bindFavoriteDatabaseStream() {
    favoriteCache.favoriteStateChangedFlowable()
        .subscribeOn(Schedulers.io())
        .subscribe {
          pushValue(RecipeCategoryListViewState.Favorited(it.second, it.first))
        }
        .addTo(disposables)
  }
}