package com.alexsullivan

import com.alexsullivan.GifRecipeProvider.GifRecipeProviderResponse
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class GifRecipeRepositoryImplTests {

  @Test
  fun `empty providers return 0 recipes`() {
    val repo = GifRecipeRepositoryImpl(listOf())
    val observer = TestObserver<GifRecipeRepository.Response>()
    repo.consumeGifRecipes(5, "").subscribe(observer)
    Assert.assertTrue(observer.awaitTerminalEvent())
    Assert.assertEquals(0, observer.valueCount())
  }

  @Test
  fun `providers are called with a proper count`() {
    val count = 10
    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProviderResponse> {
        Assert.assertEquals(count / 2, limit)
        return Observable.empty()
      }
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProviderResponse> {
        Assert.assertEquals(count / 2, limit)
        return Observable.empty()
      }
    }

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2))
    val observer = TestObserver<GifRecipeRepository.Response>()
    repo.consumeGifRecipes(10, "").subscribe(observer)
    Assert.assertTrue(observer.awaitTerminalEvent())
  }

  @Test
  fun `providers merge all of their recipes together`() {
    val provider1 = createProvider("1", 1)
    val provider2 = createProvider("2", 0)
    val provider3 = createProvider("3", 3)
    val provider4 = createProvider("4", 4)

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2, provider3, provider4))
    val observer = repo.consumeGifRecipes(100, "").test()
    Assert.assertTrue(observer.awaitTerminalEvent())
    observer.assertComplete()
    Assert.assertEquals(1, observer.valueCount())
    val recipes = observer.values().first().recipes
    Assert.assertEquals(8, recipes.size)
  }

  @Test
  fun `continuations are merged correctly`() {
    val provider1 = createProvider("1", 1)
    val provider2 = createProvider("2", 0)
    val provider3 = createProvider("3", 3)
    val provider4 = createProvider("4", 20)

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2, provider3, provider4))
    // We're asking for 20 gifs at a time. Across 4 providers, that mean each should be tasked
    // with getting 5 gifs.
    val observer = repo.consumeGifRecipes(20, "").test()
    Assert.assertTrue(observer.awaitTerminalEvent())
    observer.assertComplete()
    Assert.assertEquals(1, observer.valueCount())
    val recipes = observer.values().first().recipes
    // Each provider was tasked with getting 5 gifs. We got 1 from the first provider, 0 from the
    // second provider, 3 from the third provider, and 5 from the fourth (since each was tasked with
    // getting up to 5, so it was maxed out for the fourth provider) for a total of 9 gifs.
    Assert.assertEquals(9, recipes.size)
    // Now we're looking at the continuation, so the next round of gifs.
    val continuation = observer.values().first().continuation
    val continuationObserver = continuation.test()
    Assert.assertTrue(continuationObserver.awaitTerminalEvent())
    continuationObserver.assertComplete()
    Assert.assertEquals(1, continuationObserver.valueCount())
    val continuationRecipes = continuationObserver.values().first().recipes
    // In the last iteration we got results from the first, third, and fourth providers and nothing
    // from the second provider. So we've exhausted the second provider. Now we're going to ask
    // each provider for 20/3 gifs, so about 6 for each provider. That being said, providers
    // 1 and 3 actually don't have anything left to give (though we don't know that at this point
    // in the real code). So we'll ask each provider for 6 recipes. The last provider will be asked
    // for 8 recipes to make up the difference.
    // As a result, on our continuation run we'll get 0 + 0 + 8 recipes back.
    Assert.assertEquals(8, continuationRecipes.size)
    // Now we're looking at the second, and final, continuation.
    val secondContinuation = continuationObserver.values().first().continuation
    val secondContinuationObserver = secondContinuation.test()
    Assert.assertTrue(secondContinuationObserver.awaitTerminalEvent())
    secondContinuationObserver.assertComplete()
    Assert.assertEquals(1, secondContinuationObserver.valueCount())
    val secondContinuationRecipes = secondContinuationObserver.values().first().recipes
    // In the last continuation we exhausted the first and third providers. So all we've got left
    // is the fourth provider. As such, he gets asked for the full 20 gifs. BUT he's only got 7
    // gifs left! So we expect to get back 7 gifs.
    Assert.assertEquals(7, secondContinuationRecipes.size)
    val thirdContinuation = secondContinuationObserver.values().first().continuation
    val thirdContinuationObserver = thirdContinuation.test()
    Assert.assertTrue(thirdContinuationObserver.awaitTerminalEvent())
    thirdContinuationObserver.assertComplete()
    // And we're done!
    thirdContinuationObserver.assertComplete()
  }

  @Test
  fun `one provider ending doesnt kill the stream`() {
    val provider1 = createProvider("1", 5)
    val provider2 = createProvider("2", 1)
    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2))
    val observer = repo.consumeGifRecipes(5, "").test()
    Assert.assertTrue(observer.awaitTerminalEvent())
    observer.assertComplete()
    Assert.assertEquals(1, observer.valueCount())
    val response = observer.values().first()
    Assert.assertEquals(3, response.recipes.size)
    val continuationObserver = response.continuation.test()
    Assert.assertEquals(1, continuationObserver.valueCount())
    val continuationResponse = continuationObserver.values().first()
    // Same as above - we don't know that the second provider has finished yet, so we again
    // ask each divider for 5/2 = 2 items.
    Assert.assertEquals(2, continuationResponse.recipes.size)
    val finalContinuation = continuationResponse.continuation
    val finalResponse = finalContinuation.test()
    finalResponse.awaitTerminalEvent()
    finalResponse.assertComplete()
    Assert.assertEquals(1, finalResponse.valueCount())
    Assert.assertEquals(1, finalResponse.values().first().recipes.size)
  }

  @Test
  fun `one provider ending maintains the request count`() {
    val provider1 = createProvider("1", 45)
    val provider2 = createProvider("2", 0)
    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2))
    val observer = repo.consumeGifRecipes(15, "").test()
    Assert.assertTrue(observer.awaitTerminalEvent())
    Assert.assertEquals(1, observer.values().size)
    val recipes = observer.values().first().recipes
    Assert.assertEquals(7, recipes.size)
    val continuation = observer.values().first().continuation
    val continuationObserver = continuation.test()
    Assert.assertTrue(continuationObserver.awaitTerminalEvent())
    Assert.assertEquals(1, continuationObserver.values().size)
    Assert.assertEquals(15, continuationObserver.values().first().recipes.size)
  }


  @Test
  @Ignore("Going to postpone getting the kill stream situation resolved")
  fun testErrorDoesntKillStream() {
    val builder = fun(id: String) = GifRecipeProviderResponse(
        listOf(
            createRecipe("", id, "", ImageType.VIDEO, "", 0, "")
        ), { Observable.empty() })
    val exception = RuntimeException()
    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProviderResponse> =
          Observable.just(builder(id))
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProviderResponse> =
          Observable.error(exception)
    }

    val provider3 = object : GifRecipeProvider {
      override val id: String
        get() = "3"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProviderResponse> =
          Observable.fromArray(builder(id), builder(id), builder(id))
    }

    val provider4 = object : GifRecipeProvider {
      override val id: String
        get() = "4"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProviderResponse> =
          Observable.fromArray(builder(id), builder(id), builder(id), builder(id))
    }

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2, provider3, provider4))
    val observer = TestObserver<GifRecipeRepository.Response>()
    repo.consumeGifRecipes(5, "").subscribe(observer)
    Assert.assertEquals(8, observer.valueCount())
    Assert.assertTrue(observer.awaitTerminalEvent())
    observer.assertError(exception)
  }

  @Test
  fun `repository returns recipes chronologically`() {
    val firstProviderDates = listOf(
        16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L
    )
    val secondProviderDates = listOf(
        1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L
    )
    val provider1 = createProvider("1", 15, firstProviderDates)
    val provider2 = createProvider("2", 15, secondProviderDates)
    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2))
    val observer = repo.consumeGifRecipes(50).test()
    observer.awaitTerminalEvent()
    val returnValue = observer.values().first()
    val recipes = returnValue.recipes
    val sortedRecipes = recipes.sortedByDescending { it.creationDate }
    Assert.assertEquals(recipes, sortedRecipes)
  }

  @Test
fun `repository prioritizes newer recipes in providers`() {
    val firstProviderDates = listOf(
        1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L
    )
    val secondProviderDates = listOf(
        21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L
    )
    val thirdProviderDates = listOf(
        10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L
    )
    val provider1 = createProvider("1", 20, firstProviderDates)
    val provider2 = createProvider("2", 20, secondProviderDates)
    val provider3 = createProvider("3", 20, thirdProviderDates)
    val repository = GifRecipeRepositoryImpl(listOf(provider1, provider2, provider3))
    val firstResponse = repository.consumeGifRecipes(10).test().values().first()
    val firstRecipes = firstResponse.recipes
    Assert.assertEquals(listOf(30L, 29L, 28L, 27L, 26L, 25L, 24L, 23L, 22L, 21L), firstRecipes.map { it.creationDate })
    val secondResponse = firstResponse.continuation.test().values().first()

  }

  // TODO: I just introduced the createProvider function in TestFactories to make writing these tests
  // easier. It's shown some weirdness in the tests - specifically, the way I had written them before
  // the providers ignored the actual number of recipes that were being requested. Now they don't,
  // so I have to adjust accordingly. In the test where I used the function I set the total requested
  // recipes count super high so that each provider would exhaust its total number of recipes on the
  // first pass. I think that's fine for now, but presumably it won't work in further tests.
  // After I convert the other tests over to using the new factory method, I want to actually write
  // a (failing) test that presumes we're pulling down recipes in chronological order.
}