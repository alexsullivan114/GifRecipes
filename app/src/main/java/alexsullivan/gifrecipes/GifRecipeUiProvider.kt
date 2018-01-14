package alexsullivan.gifrecipes

import io.reactivex.Observable

interface GifRecipeUiProvider {
  fun fetchRecipes(count: Int, key: String): Observable<Pair<String?, List<GifRecipeUI>>>
}