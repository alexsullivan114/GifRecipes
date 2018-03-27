package alexsullivan.com.gfycat

import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test

class GfycatRecipeProviderTest {

  @Test
  fun `provider properly filters recipes`() {
    val service = object : GfycatService {
      override fun fetchRecipes(tagName: String, limit: Int, pageKey: String?): Observable<GfycatApiResponse> {
        val recipes = listOf<GfycatRecipe>(
            GfycatRecipe("test 1", "test", "Creative title"),
            GfycatRecipe("test 1", "test", "Chicken cutlets"),
            GfycatRecipe("test 1", "test", "Avacado pasta"),
            GfycatRecipe("test 1", "test", "Strawberry milkshake"),
            GfycatRecipe("test 1", "test", "Strawberry shortcake")
        )
        val response = GfycatApiResponse("empty", recipes.shuffled())
        return Observable.just(response)
      }
    }

    val gfycatProvider = GfycatRecipeProvider(service)

    val strawberryGfycats = gfycatProvider
        .consumeRecipes(50, "Strawberry", "test page key")
        .test()
        .values()
        .first()
        .recipes
        .map { it.title }
    Assert.assertEquals(2, strawberryGfycats.size)
    Assert.assertTrue(strawberryGfycats.contains("Strawberry shortcake"))
    Assert.assertTrue(strawberryGfycats.contains("Strawberry milkshake"))
  }

  @Test
  fun `provider doesnt filter on empty search string`() {
    val service = object: GfycatService {
      override fun fetchRecipes(tagName: String, limit: Int, pageKey: String?): Observable<GfycatApiResponse> {
        val recipes = listOf<GfycatRecipe>(
            GfycatRecipe("test 1", "test", "Creative title"),
            GfycatRecipe("test 2", "test", "Chicken cutlets"),
            GfycatRecipe("test 3", "test", "Avacado pasta"),
            GfycatRecipe("test 4", "test", "Strawberry milkshake"),
            GfycatRecipe("test 5", "test", "Strawberry shortcake")
        )
        val response = GfycatApiResponse("empty", recipes.shuffled())
        return Observable.just(response)
      }
    }

    val gfycatProvider = GfycatRecipeProvider(service)

    val strawberryGfycats = gfycatProvider
        .consumeRecipes(50, "", "test page key")
        .test()
        .values()
        .first().recipes
    Assert.assertEquals(5, strawberryGfycats.size)
  }
}