package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI

fun RecipeCategoryListViewState.reduce(new: RecipeCategoryListViewState): RecipeCategoryListViewState? {
  when (new) {
  // If we received a recipe list and our last value was loading more, we need to add all of the this
  // recipes to the new view state.
    is RecipeCategoryListViewState.RecipeList -> {
      if (this is RecipeCategoryListViewState.LoadingMore) {
        new.recipes.addAll(0, this.recipes)
      }

      return new
    }
  // If we received a loading more error, we need to add all of the this recipes in the list
  // so it can properly display everything.
    is RecipeCategoryListViewState.LoadMoreError -> {
      if (this is RecipeCategoryListViewState.LoadingMore) {
        new.recipes.addAll(this.recipes)
      }

      return new
    }
    is RecipeCategoryListViewState.Favorited -> {
      if (this is RecipeCategoryListViewState.LoadingMore || this is RecipeCategoryListViewState.RecipeList) {
        val recipes = mutableListOf<GifRecipeUI>()
        if (this is RecipeCategoryListViewState.LoadingMore) {
          recipes.addAll(this.recipes)
        } else if (this is RecipeCategoryListViewState.RecipeList) {
          recipes.addAll(this.recipes)
        }

        for ((index, value) in recipes.withIndex()) {
          if (value.id == new.recipe.id) {
            recipes[index] = value.copy(favorite = new.isFavorite)
          }
        }

        if (this is RecipeCategoryListViewState.LoadingMore) {
          return RecipeCategoryListViewState.LoadingMore(recipes)
        } else if (this is RecipeCategoryListViewState.RecipeList) {
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
