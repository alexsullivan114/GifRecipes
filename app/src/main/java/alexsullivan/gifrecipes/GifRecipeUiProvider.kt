package alexsullivan.gifrecipes

import com.alexsullivan.GifRecipeProvider
import io.reactivex.Observable

interface GifRecipeUiProvider {
  fun fetchRecipes(count: Int): Observable<Pair<Observable<GifRecipeProvider.Response>, List<GifRecipeUI>>>
  fun fetchRecipes(continuation: Observable<GifRecipeProvider.Response>): Observable<Pair<Observable<GifRecipeProvider.Response>, List<GifRecipeUI>>>
}