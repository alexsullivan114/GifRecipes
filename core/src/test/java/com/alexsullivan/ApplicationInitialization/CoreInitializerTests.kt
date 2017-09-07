package com.alexsullivan.ApplicationInitialization

import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeProvider
import com.alexsullivan.GifRecipeRegistrar
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test

class CoreInitializerTests {
    @Test fun testProperInitialization() {
        val provider1 = object: GifRecipeProvider {
            override val id: String
                get() = "1"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> =
                Observable.empty()
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
                Observable.empty()
        }

        val provider4 = object: GifRecipeProvider {
            override val id: String
                get() = "4"

            override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> =
                Observable.empty()
        }

        CoreInitializer.initialize(provider1, provider2, provider3, provider4)
        Assert.assertTrue(GifRecipeRegistrar.providers.containsAll(listOf(provider1, provider2, provider3, provider4)))
        Assert.assertEquals(GifRecipeRegistrar.providers.count(), 4)
    }
}