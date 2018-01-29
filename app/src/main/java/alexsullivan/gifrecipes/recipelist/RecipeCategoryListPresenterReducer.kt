package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.recipelist.RecipeCategoryListViewState.*

fun RecipeCategoryListViewState.reduce(new: RecipeCategoryListViewState): RecipeCategoryListViewState? {
  return when (new) {
    is LoadMoreError -> {
      if (this is LoadingMore) {
        LoadMoreError(recipes)
      }
      new
    }
    is LoadingMore -> {
      return if (this is PagingList) {
        LoadingMore(list)
      } else {
        new
      }
    }
    is PagingList -> {
      when {
        this is LoadingMore -> PagingList(this.recipes, true)
        this is PagingList -> PagingList(list, true)
        else -> new
      }
    }
    else -> {
      return new
    }
  }
}
