package com.alexsullivan.reddit.urlmanipulation

import com.alexsullivan.ImageType
import com.alexsullivan.reddit.models.RedditGifRecipe
import com.gfycat.GfycatPost
import com.gfycat.GfycatRepository
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Test

/**
 * Created by Alexs on 8/31/2017.
 */
class GfycatUrlManipulatorTests {

    private val testUrl = "test.mp4"
    private val errorMessage = "fail"

    @Test fun testHappyPathGfycatDomainMatch() {
        val manipulator = GfycatUrlManipulator(buildEmptyMockRepository())
        Assert.assertTrue(manipulator.matchesDomain("https://gfycat.com/PlayfulIckyApisdorsatalaboriosa"))
        Assert.assertTrue(manipulator.matchesDomain("https://thumbs.gfycat.com/PlayfulIckyApisdorsatalaboriosa-size_restricted.gif"))
        Assert.assertTrue(manipulator.matchesDomain("https://giant.gfycat.com/PlayfulIckyApisdorsatalaboriosa.gif"))
    }

    @Test fun testHappyPathGfycatDomainNotMatch() {
        val manipulator = GfycatUrlManipulator(buildEmptyMockRepository())
        Assert.assertFalse(manipulator.matchesDomain("https://imgur.com"))
        Assert.assertFalse(manipulator.matchesDomain("https://google.com"))
    }

    @Test fun testNotHappyPathGfycatDomainMatch() {
        val manipulator = GfycatUrlManipulator(buildEmptyMockRepository())
        Assert.assertFalse(manipulator.matchesDomain("https://imgur.com/gfycat"))
        // Don't match a URL that has no ID!
        Assert.assertFalse(manipulator.matchesDomain("https://gfycat.com"))
        Assert.assertFalse(manipulator.matchesDomain(""))
        Assert.assertFalse(manipulator.matchesDomain("https://www.google.com/search?source=hp&q=gfycat.com&oq=gfycat.com&gs_l=psy-ab.3..0l3.604.2121.0.2268.11.10.0.0.0.0.80.706.10.10.0....0...1.1.64.psy-ab..1.10.705.0..0i131k1._BSl18m4CSA"))
    }

    @Test fun testHappyPathLookup() {
        val url = "https://gfycat.com/PlayfulIckyApisdorsatalaboriosa"
        val manipulator = GfycatUrlManipulator(buildMockRepository("PlayfulIckyApisdorsatalaboriosa"))
        val recipe = RedditGifRecipe(url, "fake", ImageType.VIDEO, "fake", "fake", "fake", "fake", "")
        val testObserver = TestObserver<RedditGifRecipe>()
        manipulator.modifyRedditItem(recipe).subscribe(testObserver)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        Assert.assertTrue(testObserver.values().size == 1)
        Assert.assertEquals(testObserver.values()[0].url, testUrl)
    }

    @Test fun testHappyPathLookupAlternativeUrl() {
        val url = "https://giant.gfycat.com/PlayfulIckyApisdorsatalaboriosa"
        val manipulator = GfycatUrlManipulator(buildMockRepository("PlayfulIckyApisdorsatalaboriosa"))
        val recipe = RedditGifRecipe(url, "fake", ImageType.VIDEO, "fake", "fake", "fake", "fake", "")
        val testObserver = TestObserver<RedditGifRecipe>()
        manipulator.modifyRedditItem(recipe).subscribe(testObserver)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        Assert.assertTrue(testObserver.values().size == 1)
        Assert.assertEquals(testObserver.values()[0].url, testUrl)
    }

    @Test fun testFailedLookup() {
        val url = "https://giant.gfycat.com/PlayfulIckyApisdorsatalaboriosa"
        val manipulator = GfycatUrlManipulator(buildErrorRepository())
        val recipe = RedditGifRecipe(url, "fake", ImageType.VIDEO, "fake", "fake", "fake", "fake", "")
        val testObserver = TestObserver<RedditGifRecipe>()
        manipulator.modifyRedditItem(recipe).subscribe(testObserver)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        Assert.assertTrue(testObserver.values().size == 1)
        Assert.assertEquals(testObserver.values()[0], recipe) // we should get back the same item.
    }

    private fun buildEmptyMockRepository(): GfycatRepository {
        return object : GfycatRepository {
            override fun getImageInfo(imageId: String): Observable<GfycatPost> = Observable.empty()
        }
    }

    private fun buildMockRepository(id: String): GfycatRepository {
        return object: GfycatRepository {
            override fun getImageInfo(imageId: String): Observable<GfycatPost> {
                Assert.assertEquals("id != imageid", id, imageId)
                return Observable.just(GfycatPost(testUrl))
            }
        }
    }

    private fun buildErrorRepository(): GfycatRepository {
        return object: GfycatRepository {
            override fun getImageInfo(imageId: String): Observable<GfycatPost> =
                    Observable.error(RuntimeException(errorMessage))
        }
    }
}