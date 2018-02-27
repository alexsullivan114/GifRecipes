package com.alexsullivan

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class GifRecipeRepositoryImplTests {

  @Test
  fun testEmptyConsumption() {
    val repo = GifRecipeRepositoryImpl(listOf())
    val observer = TestObserver<GifRecipeProvider.Response>()
    repo.consumeGifRecipes(5, "").subscribe(observer)
    Assert.assertTrue(observer.awaitTerminalEvent())
    Assert.assertEquals(0, observer.valueCount())
  }

  @Test
  fun testProperCountCalled() {
    val count = 10
    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> {
        Assert.assertEquals(count / 2, limit)
        return Observable.empty()
      }
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> {
        Assert.assertEquals(count / 2, limit)
        return Observable.empty()
      }
    }

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2))
    val observer = TestObserver<GifRecipeProvider.Response>()
    repo.consumeGifRecipes(10, "").subscribe(observer)
    Assert.assertTrue(observer.awaitTerminalEvent())
  }

  @Test
  fun testProperMerging() {
    fun builder(vararg ids: String): GifRecipeProvider.Response {
     return GifRecipeProvider.Response(
         ids.map { GifRecipe("", it, "", ImageType.VIDEO, "") }
         , Observable.defer { Observable.just(builder(*ids)) })
    }
    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.just(builder(id))
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.empty()
    }

    val provider3 = object : GifRecipeProvider {
      override val id: String
        get() = "3"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.fromArray(builder(id, id, id))
    }

    val provider4 = object : GifRecipeProvider {
      override val id: String
        get() = "4"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.fromArray(builder(id, id, id, id))
    }

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2, provider3, provider4))
    val observer = repo.consumeGifRecipes(5, "").test()
    Assert.assertTrue(observer.awaitTerminalEvent())
    observer.assertComplete()
    Assert.assertEquals(1, observer.valueCount())
    val recipes = observer.values().first().recipes
    Assert.assertEquals(8, recipes.size)
  }

  @Test
  fun testProperContinuationMerging() {
    fun builder(vararg ids: String): GifRecipeProvider.Response{
      return GifRecipeProvider.Response(
          ids.map { GifRecipe("", it, "", ImageType.VIDEO, "") }
          , Observable.defer { Observable.just(builder(*ids)) })
    }

    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.just(builder(id))
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.empty()
    }

    val provider3 = object : GifRecipeProvider {
      override val id: String
        get() = "3"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.fromArray(builder(id, id, id))
    }

    val provider4 = object : GifRecipeProvider {
      override val id: String
        get() = "4"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.fromArray(builder(id, id, id, id))
    }

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2, provider3, provider4))
    val observer = repo.consumeGifRecipes(5, "").test()
    Assert.assertTrue(observer.awaitTerminalEvent())
    observer.assertComplete()
    Assert.assertEquals(1, observer.valueCount())
    val recipes = observer.values().first().recipes
    Assert.assertEquals(8, recipes.size)
    val continuation = observer.values().first().continuation
    val continuationObserver = continuation.test()
    Assert.assertTrue(continuationObserver.awaitTerminalEvent())
    continuationObserver.assertComplete()
    Assert.assertEquals(1, continuationObserver.valueCount())
    val continuationRecipes = continuationObserver.values().first().recipes
    Assert.assertEquals(8, continuationRecipes.size)
  }


  @Test
  @Ignore("Going to postpone getting the kill stream situation resolved")
  fun testErrorDoesntKillStream() {
    val builder = fun(id: String) = GifRecipeProvider.Response(
        listOf(
            GifRecipe("", id, "", ImageType.VIDEO, "")
        ), Observable.empty())
    val exception = RuntimeException()
    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.just(builder(id))
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.error(exception)
    }

    val provider3 = object : GifRecipeProvider {
      override val id: String
        get() = "3"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.fromArray(builder(id), builder(id), builder(id))
    }

    val provider4 = object : GifRecipeProvider {
      override val id: String
        get() = "4"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProvider.Response> =
          Observable.fromArray(builder(id), builder(id), builder(id), builder(id))
    }

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2, provider3, provider4))
    val observer = TestObserver<GifRecipeProvider.Response>()
    repo.consumeGifRecipes(5, "").subscribe(observer)
    Assert.assertEquals(8, observer.valueCount())
    Assert.assertTrue(observer.awaitTerminalEvent())
    observer.assertError(exception)
  }
}