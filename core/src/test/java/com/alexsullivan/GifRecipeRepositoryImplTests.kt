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
    val observer = TestObserver<GifRecipeRepository.Response>()
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

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> {
        Assert.assertEquals(count / 2, limit)
        return Observable.empty()
      }
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> {
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
  fun testProperMerging() {
    fun builder(vararg ids: String): GifRecipeRepository.Response {
      return GifRecipeRepository.Response(
          ids.map { GifRecipe("", it, "", ImageType.VIDEO, "") }
          , Observable.defer { Observable.just(builder(*ids)) })
    }

    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
          Observable.just(builder(id))
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
          Observable.empty()
    }

    val provider3 = object : GifRecipeProvider {
      override val id: String
        get() = "3"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
          Observable.fromArray(builder(id, id, id))
    }

    val provider4 = object : GifRecipeProvider {
      override val id: String
        get() = "4"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
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
    fun builder(vararg ids: String): GifRecipeRepository.Response {
      return GifRecipeRepository.Response(
          ids.map { GifRecipe("", it, "", ImageType.VIDEO, "") }
          , Observable.defer { Observable.just(builder(*ids)) })
    }

    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
          Observable.just(builder(id))
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
          Observable.empty()
    }

    val provider3 = object : GifRecipeProvider {
      override val id: String
        get() = "3"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
          Observable.fromArray(builder(id, id, id))
    }

    val provider4 = object : GifRecipeProvider {
      override val id: String
        get() = "4"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
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
  fun testOneProviderEndingDoesntEndAll() {
    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> {
        val recipes = listOf(createRecipe(), createRecipe(), createRecipe(), createRecipe())
        val continuationRecipes = listOf(createRecipe(), createRecipe(), createRecipe(), createRecipe(), createRecipe())
        val emptyObservable = Observable.empty<GifRecipeRepository.Response>()
        return Observable.just(GifRecipeRepository.Response(recipes, Observable.just(GifRecipeRepository.Response(continuationRecipes, emptyObservable))))
      }
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> {
        val recipes = listOf(createRecipe(), createRecipe(), createRecipe(), createRecipe())
        return Observable.just(GifRecipeRepository.Response(recipes, Observable.empty()))
      }
    }

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2))
    val observer = repo.consumeGifRecipes(5, "").test()
    Assert.assertTrue(observer.awaitTerminalEvent())
    observer.assertComplete()
    Assert.assertEquals(1, observer.valueCount())
    val response = observer.values().first()
    Assert.assertEquals(8, response.recipes.size)
    val continuationObserver = response.continuation.test()
    Assert.assertEquals(1, continuationObserver.valueCount())
    val continuationResponse = continuationObserver.values().first()
    Assert.assertEquals(5, continuationResponse.recipes.size)
  }

  @Test
  fun testOneProviderEndingMaintainsRequestCount() {
    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> {
        val recipes = mutableListOf<GifRecipe>()
        for (i in 0..limit) {
          recipes.add(createRecipe())
        }
        val continuationRecipes = mutableListOf<GifRecipe>()
        for (i in 0..limit) {
          continuationRecipes.add(createRecipe())
        }
        val emptyObservable = Observable.empty<GifRecipeRepository.Response>()
        return Observable.just(GifRecipeRepository.Response(recipes, Observable.just(GifRecipeRepository.Response(continuationRecipes, emptyObservable))))
      }
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> {
        return Observable.just(GifRecipeRepository.Response(emptyList(), Observable.empty()))
      }
    }

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2))
    val observer = repo.consumeGifRecipes(15, "").test()
    Assert.assertTrue(observer.awaitTerminalEvent())
    Assert.assertEquals(1, observer.values().size)
    val continuation = observer.values().first().continuation
    val continuationObserver = continuation.test()
    Assert.assertTrue(continuationObserver.awaitTerminalEvent())
    Assert.assertEquals(1, continuationObserver.values().size)
    Assert.assertEquals(15, continuationObserver.values().first().recipes.size)
  }


  @Test
  @Ignore("Going to postpone getting the kill stream situation resolved")
  fun testErrorDoesntKillStream() {
    val builder = fun(id: String) = GifRecipeRepository.Response(
        listOf(
            GifRecipe("", id, "", ImageType.VIDEO, "")
        ), Observable.empty())
    val exception = RuntimeException()
    val provider1 = object : GifRecipeProvider {
      override val id: String
        get() = "1"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
          Observable.just(builder(id))
    }

    val provider2 = object : GifRecipeProvider {
      override val id: String
        get() = "2"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
          Observable.error(exception)
    }

    val provider3 = object : GifRecipeProvider {
      override val id: String
        get() = "3"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
          Observable.fromArray(builder(id), builder(id), builder(id))
    }

    val provider4 = object : GifRecipeProvider {
      override val id: String
        get() = "4"

      override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeRepository.Response> =
          Observable.fromArray(builder(id), builder(id), builder(id), builder(id))
    }

    val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2, provider3, provider4))
    val observer = TestObserver<GifRecipeRepository.Response>()
    repo.consumeGifRecipes(5, "").subscribe(observer)
    Assert.assertEquals(8, observer.valueCount())
    Assert.assertTrue(observer.awaitTerminalEvent())
    observer.assertError(exception)
  }
}