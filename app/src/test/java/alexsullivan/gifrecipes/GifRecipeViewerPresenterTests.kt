package alexsullivan.gifrecipes

import alexsullivan.gifrecipes.GifRecipeViewerViewState.*
import alexsullivan.testutils.CacheServerAdapter
import alexsullivan.testutils.FavoriteCacheAdapter
import alexsullivan.testutils.ReactiveTestFavoriteCache
import com.alexsullivan.ImageType
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class GifRecipeViewerPresenterTests {

    @Test
    fun testFavoritingWhileLoading() {
        val scheduler = TestScheduler()
        val recipe = GifRecipeUI("https://fake.com", "fake", "https://fakethumbnail.com", ImageType.VIDEO, "fake title")
        var presenter: GifRecipeViewerPresenter? = null
        val cacheServer = object: CacheServerAdapter() {
            override fun isCached(url: String) = false
            override fun cacheProgress(url: String): Observable<Int>{
                return Observable.just(1,2,3,4,5)
                    .doOnNext {
                        if (it == 3) presenter?.favoriteClicked(true)
                        scheduler.triggerActions()
                    }
            }
        }
        val favoriteCache = ReactiveTestFavoriteCache()
        presenter = GifRecipeViewerPresenterImpl(recipe, cacheServer, favoriteCache, scheduler)
        val stream = presenter.stateStream.test()
        scheduler.triggerActions()
        val values = stream.values()
        assertEquals("Should have 9 values including re-emmited favorited state", 9, values.size)

        val buildLoadingState = fun(progress: Int, favorited: Boolean)
            = LoadingVideo(progress, recipe.copy(favorite = favorited), url = recipe.url,
                hasTransitioned = false, favoriteLocked = !favorited)
        val preloading = Preloading(recipe)
        val playingState = PlayingVideo(recipe.url, recipe.copy(favorite = true), favoriteLocked = false)
        Assert.assertEquals("Proper preloading", preloading, values[0])
        Assert.assertEquals("Proper Loading 0", buildLoadingState(0, false), values[1])
        Assert.assertEquals("Proper Loading 1", buildLoadingState(1, false), values[2])
        Assert.assertEquals("Proper Loading 2", buildLoadingState(2, false), values[3])
        Assert.assertEquals("Favorited Loading 3", buildLoadingState(2, true), values[4])
        Assert.assertEquals("Favorited Loading 4", buildLoadingState(3, true), values[5])
        Assert.assertEquals("Favorited Loading 5", buildLoadingState(4, true), values[6])
        Assert.assertEquals("Favorited Loading 5", buildLoadingState(5, true), values[7])
        Assert.assertEquals("Proper playing state", playingState, values[8])
    }

    @Test
    fun testPlayingVideoWhileLoading() {
        val scheduler = TestScheduler()
        val recipe = GifRecipeUI("https://fake.com", "fake", "https://fakethumbnail.com", ImageType.VIDEO, "fake title")
        var presenter: GifRecipeViewerPresenter? = null
        val cacheServer = object: CacheServerAdapter() {
            override fun isCached(url: String) = false
            override fun cacheProgress(url: String): Observable<Int> {
                return Observable.just(1,2,3,4,5)
                    // Halfway through our loading progress call out to our presenter.
                    .doOnNext { if (it == 3) presenter?.videoStarted() }
            }
        }
        val favoriteCache = object: FavoriteCacheAdapter(){}
        presenter = GifRecipeViewerPresenterImpl(recipe, cacheServer, favoriteCache, scheduler)
        val stream = presenter.stateStream.test()
        // Await our first loading items.
        scheduler.triggerActions()
        val values = stream.values()
        assertEquals("Should have 9 values including transition value", 9, values.size)

        val buildLoadingState = fun(progress: Int, transitioned: Boolean)
            = LoadingVideo(progress, recipe, url = recipe.url, hasTransitioned = transitioned)
        val preloading = Preloading(recipe)
        val playingState = PlayingVideo(recipe.url, recipe)
        val transitionState = TransitioningVideo(recipe, url = recipe.url, progress = 3)
        Assert.assertEquals("Proper preloading", preloading, values[0])
        Assert.assertEquals("Proper Loading 0", buildLoadingState(0, false), values[1])
        Assert.assertEquals("Proper Loading 1", buildLoadingState(1, false), values[2])
        Assert.assertEquals("Proper Loading 2", buildLoadingState(2, false), values[3])
        Assert.assertEquals("Proper Loading 3", buildLoadingState(3, false), values[4])
        Assert.assertEquals("Transition state", transitionState, values[5])
        Assert.assertEquals("Proper Loading 4", buildLoadingState(4, true), values[6])
        Assert.assertEquals("Proper Loading 5", buildLoadingState(5, true), values[7])
        Assert.assertEquals("Proper playing state", playingState, values[8])
    }

    @Test
    fun testNonCachedRecipeVideoInitialState() {
        val buildLoadingState = fun(progress: Int, recipe: GifRecipeUI, url: String) =
            LoadingVideo(progress, recipe, url = url, hasTransitioned = false)
        val playingState = fun(recipe: GifRecipeUI, url: String) =
            PlayingVideo(url, recipe)
        testNonCachedRecipeInitialState(ImageType.VIDEO, buildLoadingState, playingState)
    }

    @Test
    fun testNonCachedRecipeGifInitialState() {
        val buildLoadingState = fun(progress: Int, recipe: GifRecipeUI, url: String) = LoadingGif(progress, recipe, url = url)
        val playingState = fun(recipe: GifRecipeUI, url: String) =
            PlayingGif(url, recipe)
        testNonCachedRecipeInitialState(ImageType.GIF, buildLoadingState, playingState)
    }

    fun testNonCachedRecipeInitialState(imageType: ImageType,
                                        loadingState: (Int, GifRecipeUI, String) -> GifRecipeViewerViewState,
                                        playingState: (GifRecipeUI, String) -> GifRecipeViewerViewState) {
        val scheduler = TestScheduler()
        val recipe = GifRecipeUI("https://fake.com", "fake", "https://fakethumbnail.com", imageType, "fake title")
        val cacheServer = object: CacheServerAdapter() {
            override fun isCached(url: String) = false
            override fun cacheProgress(url: String) = Observable.just(1,2,3,4,5)
        }
        val favoriteCache = object: FavoriteCacheAdapter(){}
        val presenter = GifRecipeViewerPresenterImpl(recipe, cacheServer, favoriteCache, scheduler)
        val stream = presenter.stateStream.test()
        // Await our first loading items.
        scheduler.triggerActions()
        val values = stream.values()
        assertEquals("Should have 8 loading values", 8, values.size)

        val preloading = Preloading(recipe)
        Assert.assertEquals("Proper preloading", preloading, values[0])
        Assert.assertEquals("Proper Loading 0", loadingState(0, recipe, recipe.url), values[1])
        Assert.assertEquals("Proper Loading 1", loadingState(1, recipe, recipe.url), values[2])
        Assert.assertEquals("Proper Loading 2", loadingState(2, recipe, recipe.url), values[3])
        Assert.assertEquals("Proper Loading 3", loadingState(3, recipe, recipe.url), values[4])
        Assert.assertEquals("Proper Loading 4", loadingState(4, recipe, recipe.url), values[5])
        Assert.assertEquals("Proper Loading 5", loadingState(5, recipe, recipe.url), values[6])
        Assert.assertEquals("Proper playing state", playingState(recipe, recipe.url), values[7])
    }

    @Test
    fun testCachedRecipeInitialState() {
        val scheduler = TestScheduler()
        val videoRecipe = GifRecipeUI("https://fake.com", "fake", "https://fakethumbnail.com", ImageType.VIDEO, "fake title")
        val cacheServer = object: CacheServerAdapter() {
            override fun isCached(url: String) = true
        }
        val favoriteCache = object: FavoriteCacheAdapter(){}

        val presenter = GifRecipeViewerPresenterImpl(videoRecipe, cacheServer, favoriteCache, scheduler)
        val stateStream = presenter.stateStream.test()
        scheduler.triggerActions()
        val values = stateStream.values()
        assertEquals("State stream should have one playing value", 1, values.size)
        assertEquals("State streams value should be playing video",
            PlayingVideo("https://fake.com", videoRecipe), values[0])
        val gifRecipe = GifRecipeUI("https://fake.com", "fake", "https://fakethumbnail.com", ImageType.GIF, "fake title")
        val gifPresenter = GifRecipeViewerPresenterImpl(gifRecipe, cacheServer, favoriteCache, scheduler)
        val gifStateStream = gifPresenter.stateStream.test()
        scheduler.triggerActions()
        val gifValues = gifStateStream.values()
        assertEquals("State stream should have one playing value", 1, gifValues.size)
        assertEquals("State streams value should be playing gif", PlayingGif("https://fake.com", gifRecipe), gifValues[0])
    }

}