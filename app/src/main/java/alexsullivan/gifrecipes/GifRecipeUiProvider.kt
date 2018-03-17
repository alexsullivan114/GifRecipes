package alexsullivan.gifrecipes

import com.alexsullivan.GifRecipeRepository
import io.reactivex.Observable

interface GifRecipeUiProvider {
  fun fetchRecipes(count: Int): Observable<Pair<Observable<GifRecipeRepository.Response>, List<GifRecipeUI>>>
  fun fetchRecipes(continuation: Observable<GifRecipeRepository.Response>): Observable<Pair<Observable<GifRecipeRepository.Response>, List<GifRecipeUI>>>
}