package com.alexsullivan.reddit

import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeProvider
import com.alexsullivan.ImageType
import com.alexsullivan.logging.Logger
import com.alexsullivan.reddit.models.RedditGifRecipe
import com.alexsullivan.reddit.models.RedditListingItem
import com.alexsullivan.reddit.models.RedditListingResponse
import com.alexsullivan.reddit.models.RedditListingResponseData
import com.alexsullivan.reddit.network.RedditService
import com.alexsullivan.reddit.testutils.EmptyLogger
import com.alexsullivan.reddit.urlmanipulation.UrlManipulator
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert
import org.junit.Test

/**
 * Created by Alexs on 8/31/2017.
 */
class RedditGifRecipeProviderTests {

    @Test fun simpleResponse() {
        val titles = listOf("one", "two", "three", "four", "five")
        val ids = listOf("id1", "id2", "id3", "id4", "id5")
        val urls = listOf("Url one", "Url two", "Url three", "Url four", "Url five")
        val count = 5
        val key = "key"
        val service = buildService(ids, urls, titles, count, key)
        val scheduler = TestScheduler()
        val gifRecipeProvider = buildProvider(service = service, scheduler = scheduler)
        val testObserver = TestObserver<GifRecipeProvider.Response>()
        gifRecipeProvider.consumeRecipes(count, "", "").subscribe(testObserver)
        scheduler.triggerActions()
        // Make sure we finish.
        Assert.assertTrue(testObserver.awaitTerminalEvent())
        // Make sure we only have our five desired elements.
        Assert.assertTrue(testObserver.valueCount() == 1)
        val recipes = testObserver.values().first().recipes
        // Make sure all of our titles are contained. We don't know what order they'll come in.
        Assert.assertTrue(recipes.map { it.title }.containsAll(titles))
        // Let's examine a single recipe and make sure it has everything we expect.
        val firstRecipe = recipes.first { it.title == "one" }
        Assert.assertNotNull(firstRecipe)
        Assert.assertEquals(ids[0], firstRecipe.id)
        Assert.assertEquals(urls[0], firstRecipe.url)
        // Default should be gif.
        Assert.assertEquals(ImageType.GIF, firstRecipe.imageType)
    }

    @Test fun simpleVideoResponse() {
        val count = 5
        val service = buildService(count = count, key = "")
        val scheduler = TestScheduler()
        val manipulator = object: UrlManipulator {
            override fun matchesDomain(domain: String) = true

            override fun modifyRedditItem(item: RedditGifRecipe) =
                Observable.just(item.copy(imageType = ImageType.VIDEO))
        }
        val gifRecipeProvider = buildProvider(service = service, scheduler = scheduler, urlManipulator = listOf(manipulator))
        val testObserver = TestObserver<GifRecipe>()
        gifRecipeProvider.consumeRecipes(count, "", "").subscribe(testObserver)
        scheduler.triggerActions()
        // Make sure we finish.
        Assert.assertTrue(testObserver.awaitTerminalEvent())
        // Make sure all of our recipes are of the video image type now.
        Assert.assertEquals(count, testObserver.values().filter { it.imageType == ImageType.VIDEO }.size)
    }

    @Test fun testFilterRecipes() {
        val urls = listOf("Url one", "Url two", "Url three", "Url four", "Url five")
        val count = 5
        val service = buildService(urls = urls, count = count, key = "")
        val scheduler = TestScheduler()
        // Filter out everything but the first two urls.
        val imageChecker = fun(url: String) = url == urls[0] || url == urls[1]
        val gifRecipeProvider = buildProvider(service = service, scheduler = scheduler, mediaChecker = imageChecker)
        val testObserver = TestObserver<GifRecipe>()
        gifRecipeProvider.consumeRecipes(count, "foo", "").subscribe(testObserver)
        scheduler.triggerActions()
        // Make sure we finish.
        Assert.assertTrue(testObserver.awaitTerminalEvent())
        // Now make sure we're only left with 2 recipes.
        Assert.assertEquals(2, testObserver.valueCount())
        // Make sure those two items are what we expect.
        val returnedUrls = testObserver.values().map { it.url }
        Assert.assertTrue(returnedUrls.contains(urls[0]))
        Assert.assertTrue(returnedUrls.contains(urls[1]))
    }

    @Test fun testProperServiceMethodCalled() {
        // Test that if we have no search term we call through to hot recipes.
        val hotOnlyService = buildEmptyService({}, {Assert.fail("Called search method on hot only service")})
        val scheduler = TestScheduler()
        val hotProvider = buildProvider(service = hotOnlyService, scheduler = scheduler)
        val hotTestObserver = TestObserver<GifRecipe>()
        hotProvider.consumeRecipes(5, "", "").subscribe(hotTestObserver)
        scheduler.triggerActions()
        Assert.assertTrue(hotTestObserver.awaitTerminalEvent())
        // Test that if we have a search term we call through to search recipes.
        val searchOnlyService = buildEmptyService({Assert.fail("Called hot method on search only service")}, {})
        val searchProvider = buildProvider(service = searchOnlyService, scheduler = scheduler)
        val searchTestObserver = TestObserver<GifRecipe>()
        searchProvider.consumeRecipes(5, "TestSearhTerm", "").subscribe(searchTestObserver)
        scheduler.triggerActions()
        Assert.assertTrue(searchTestObserver.awaitTerminalEvent())
    }

    @Test fun testFilterOutRemovedPosts() {
        val ids = listOf("1", "2", "3", "4", "5")
        val service = buildService(ids = ids, removedIds = listOf("2", "3"), count = 5, key = "")
        val testScheduler = TestScheduler()
        val provider = buildProvider(service, testScheduler)
        val testObserver = TestObserver<GifRecipe>()
        provider.consumeRecipes(5, "", "").subscribe(testObserver)
        testScheduler.triggerActions()
        Assert.assertTrue(testObserver.awaitTerminalEvent())
        Assert.assertEquals(3, testObserver.valueCount())
        Assert.assertFalse(testObserver.values().map { it.id }.contains("2"))
        Assert.assertFalse(testObserver.values().map { it.id }.contains("3"))
    }

    @Test fun testUrlManipulatorModification() {
        val manipular = object: UrlManipulator {
            override fun matchesDomain(domain: String) = domain == "TestDomain"
            override fun modifyRedditItem(item: RedditGifRecipe) = Observable.just(item.copy(url = "Modified"))
        }

        val service = buildService(urls = listOf("TestDomain", "NonTestDomain"), count = 2, key = "")
        val testScheduler = TestScheduler()
        val provider = buildProvider(service, testScheduler, urlManipulator = listOf(manipular))
        val testObserver = TestObserver<GifRecipe>()
        provider.consumeRecipes(2, "", "").subscribe(testObserver)
        testScheduler.triggerActions()
        Assert.assertTrue(testObserver.awaitTerminalEvent())
        Assert.assertEquals("Modified", testObserver.values().map { it.url }.first { it == "Modified" })
    }

    @Test fun testUrlManipulatorErrorCaseHandled() {
        val manipular = object: UrlManipulator {
            override fun matchesDomain(domain: String) = domain == "TestDomain"
            override fun modifyRedditItem(item: RedditGifRecipe) = Observable.error<RedditGifRecipe>(RuntimeException())
        }

        val service = buildService(urls = listOf("TestDomain", "NonTestDomain"), count = 2, key = "")
        val testScheduler = TestScheduler()
        val provider = buildProvider(service, testScheduler, urlManipulator = listOf(manipular))
        val testObserver = TestObserver<GifRecipe>()
        provider.consumeRecipes(2, "", "").subscribe(testObserver)
        testScheduler.triggerActions()
        Assert.assertTrue(testObserver.awaitTerminalEvent())
    }

    private fun buildProvider(service: RedditService, scheduler: Scheduler, urlManipulator: List<UrlManipulator> = listOf(),
                              mediaChecker: (url: String) -> Boolean = fun(_: String) = true,
                              logger: Logger = EmptyLogger) = RedditGifRecipeProviderImpl(service, urlManipulator, mediaChecker, logger, scheduler, "fake")

    private fun buildService(ids: List<String> = listOf(),
                             urls: List<String> = listOf(),
                             titles: List<String> = listOf(),
                             count: Int,
                             key: String, removedIds: List<String> = listOf()): RedditService {

        val items = mutableListOf<RedditListingItem>()
        for (i in 0 until count) {
            val id = ids.elementAtOrElse(i, {"id${i+1}"})
            val item = RedditListingItem("t3", id, urls.elementAtOrElse(i, {"url${i+1}"}), "Gfycat", "", "",
                titles.elementAtOrElse(i, {"Test Title ${i+1}"}), "key${i+1}", removedIds.contains(id))
            items.add(i, item)
        }

        return object: RedditService {
            override fun hotRecipes(subreddit: String, limit: Int, after: String?): Observable<RedditListingResponse> {
                val responeData = RedditListingResponseData("", items, "", key)
                val response = RedditListingResponse("post", responeData)
                return Observable.just(response)
            }

            override fun searchRecipes(subreddit: String, searchParam: String, after: String?, limit: Int, restrict: Boolean?, sort: String, useRawJson: Int): Observable<RedditListingResponse> {
                val responeData = RedditListingResponseData("", items, "", key)
                val response = RedditListingResponse("post", responeData)
                return Observable.just(response)
            }
        }
    }

    private fun buildEmptyService(hotBlock: () -> Unit, searchBlock: () -> Unit): RedditService {
        return object: RedditService {
            override fun hotRecipes(subreddit: String, limit: Int, after: String?): Observable<RedditListingResponse> {
                hotBlock()
                return Observable.empty()
            }

            override fun searchRecipes(subreddit: String, searchParam: String, after: String?, limit: Int, restrict: Boolean?, sort: String, useRawJson: Int): Observable<RedditListingResponse> {
                searchBlock()
                return Observable.empty()
            }
        }
    }
}