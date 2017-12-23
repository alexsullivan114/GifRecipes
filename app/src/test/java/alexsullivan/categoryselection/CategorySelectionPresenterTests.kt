package alexsullivan.categoryselection

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.categoryselection.CategorySelectionPresenterImpl
import alexsullivan.gifrecipes.categoryselection.CategorySelectionViewState.*
import alexsullivan.gifrecipes.utils.toGifRecipe
import alexsullivan.testutils.EmptyLogger
import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeRepository
import com.alexsullivan.ImageType
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CategorySelectionPresenterTests {

    @Test
    fun testNormalFlow() {
        val recipeBuilder = fun() = GifRecipeUI("fake", "fake", "fake", ImageType.VIDEO, "fake")
        val testScheduler = TestScheduler()
        val recipeList = listOf(recipeBuilder(),recipeBuilder(),recipeBuilder(),recipeBuilder(),recipeBuilder())
        val recipeRepository = object: GifRecipeRepository {
            override fun consumeGifRecipes(totalDesiredGifs: Int, searchTerm: String, pageKey: String) =
                Observable.fromIterable(recipeList)
                    .map { it.toGifRecipe() }
        }

        val presenter = CategorySelectionPresenterImpl(recipeRepository, testScheduler, EmptyLogger)
        val stream = presenter.stateStream.test()
        testScheduler.triggerActions()
        stream.assertNoErrors()
        val values = stream.values()
        assertEquals("Expected Fetching and GifList view states", 2, values.size)
        assertTrue("First value should be a loading value", values[0] is FetchingGifs)
        assertEquals("Second value should be a list of recipes", GifList(recipeList), values[1])
    }

    @Test
    fun testNetworkError() {
        val testScheduler = TestScheduler()
        val repository = object: GifRecipeRepository {
            override fun consumeGifRecipes(totalDesiredGifs: Int, searchTerm: String, pageKey: String) =
                Observable.error<GifRecipe>(IOException())
        }
        val presenter = CategorySelectionPresenterImpl(repository, testScheduler, EmptyLogger)
        val stream = presenter.stateStream.test()
        testScheduler.triggerActions()
        stream.assertNoErrors()
        val values = stream.values()
        assertEquals("Expected loading and error state", 2, values.size)
        assertTrue("Expected loading first", values[0] is FetchingGifs)
        assertTrue("Expected Network Error", values[1] is NetworkError)
    }

    @Test
    fun testNonNetworkError() {
        val testScheduler = TestScheduler()
        val repository = object: GifRecipeRepository {
            override fun consumeGifRecipes(totalDesiredGifs: Int, searchTerm: String, pageKey: String) =
                Observable.error<GifRecipe>(RuntimeException())
        }
        val presenter = CategorySelectionPresenterImpl(repository, testScheduler, EmptyLogger)
        val stream = presenter.stateStream.test()
        testScheduler.triggerActions()
        stream.assertError(RuntimeException())
        val values = stream.values()
//        assertEquals("Expected loading and error state", 2, values.size)
//        assertTrue("Expected loading first", values[0] is FetchingGifs)
//        assertTrue("Expected Network Error", values[1] is NetworkError)
    }

}