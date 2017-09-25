package com.alexsullivan

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Test

class GifRecipeRepositoryImplTests {
    @Test fun testEmptyConsumption() {
        val repo = GifRecipeRepositoryImpl(listOf())
        val observer = TestObserver<GifRecipe>()
        repo.consumeGifRecipes(5, "", "").subscribe(observer)
        Assert.assertTrue(observer.awaitTerminalEvent())
        Assert.assertEquals(0, observer.valueCount())
    }

    @Test fun testProperCountCalled() {
        val count = 10
        val provider1 = object: GifRecipeProvider {
            override val id: String
                get() = "1"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> {
                Assert.assertEquals(count / 2, limit)
                return Observable.empty()
            }
        }

        val provider2 = object: GifRecipeProvider {
            override val id: String
                get() = "2"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> {
                Assert.assertEquals(count / 2, limit)
                return Observable.empty()
            }
        }

        val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2))
        val observer = TestObserver<GifRecipe>()
        repo.consumeGifRecipes(10, "", "").subscribe(observer)
        Assert.assertTrue(observer.awaitTerminalEvent())
    }

    @Test fun testProperMerging() {
        val builder = fun (id: String) = GifRecipe("", id, "", ImageType.VIDEO, "", "")
        val provider1 = object: GifRecipeProvider {
            override val id: String
                get() = "1"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> =
                Observable.just(builder(id))
        }

        val provider2 = object: GifRecipeProvider {
            override val id: String
                get() = "2"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> =
                Observable.empty()
        }

        val provider3 = object: GifRecipeProvider {
            override val id: String
                get() = "3"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> =
                Observable.fromArray(builder(id), builder(id), builder(id))
        }

        val provider4 = object: GifRecipeProvider {
            override val id: String
                get() = "4"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> =
                Observable.fromArray(builder(id), builder(id), builder(id), builder(id))
        }

        val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2, provider3, provider4))
        val observer = repo.consumeGifRecipes(5, "", "").test()
        Assert.assertTrue(observer.awaitTerminalEvent())
        observer.assertComplete()
        Assert.assertEquals(8, observer.valueCount())
    }

    @Test fun testErrorDoesntKillStream() {
        val builder = fun (id: String) = GifRecipe("", id, "", ImageType.VIDEO, "", "")
        val exception = RuntimeException()
        val provider1 = object: GifRecipeProvider {
            override val id: String
                get() = "1"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> =
                Observable.just(builder(id))
        }

        val provider2 = object: GifRecipeProvider {
            override val id: String
                get() = "2"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> =
                Observable.error(exception)
        }

        val provider3 = object: GifRecipeProvider {
            override val id: String
                get() = "3"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> =
                Observable.fromArray(builder(id), builder(id), builder(id))
        }

        val provider4 = object: GifRecipeProvider {
            override val id: String
                get() = "4"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> =
                Observable.fromArray(builder(id), builder(id), builder(id), builder(id))
        }

        val repo = GifRecipeRepositoryImpl(listOf(provider1, provider2, provider3, provider4))
        val observer = TestObserver<GifRecipe>()
        repo.consumeGifRecipes(5, "", "").subscribe(observer)
        Assert.assertEquals(8, observer.valueCount())
        Assert.assertTrue(observer.awaitTerminalEvent())
        observer.assertError(exception)
    }
}