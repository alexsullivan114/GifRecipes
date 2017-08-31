package com.alexsullivan.reddit.urlmanipulation

import com.alexsullivan.ImageType
import com.alexsullivan.reddit.models.RedditGifRecipe
import com.gfycat.ImgurPost
import com.gfycat.ImgurRepository
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Test

/**
 * Created by Alexs on 8/31/2017.
 */
class ImgurUrlManipulatorTests {

    private val testUrl = "test.mp4"
    private val errorMessage = "fail"

    @Test fun testHappyPathImgurDomainMatch() {
        val manipulator = ImgurUrlManipulator(buildEmptyRepository())
        Assert.assertTrue(manipulator.matchesDomain("imgur.com/sefes"))
        Assert.assertTrue(manipulator.matchesDomain("i.imgur.com/sefes"))
    }

    @Test fun testHappyPathImgurDomainNonMatch() {
        val manipulator = ImgurUrlManipulator(buildEmptyRepository())
        Assert.assertFalse(manipulator.matchesDomain("imgur.com"))
        Assert.assertFalse(manipulator.matchesDomain("www.google.com"))
        Assert.assertFalse(manipulator.matchesDomain("www.google.com/i.imgur.com/fsef"))
    }

    @Test fun testHappyPathLookup() {
        val url = "https://imgur.com/sef12fsefes"
        val manipulator = ImgurUrlManipulator(buildMockRepository("sef12fsefes"))
        val recipe = RedditGifRecipe(url, "fake", ImageType.VIDEO, "fake", "fake", "fake", "fake", "")
        val testObserver = TestObserver<RedditGifRecipe>()
        manipulator.modifyRedditItem(recipe).subscribe(testObserver)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        Assert.assertTrue(testObserver.values().size == 1)
        Assert.assertEquals(testObserver.values()[0].url, testUrl)
    }

    @Test fun testHappyPathLookupAlternativeUrl() {
        val url = "https://imgur.com/PlayfulIckyApisdorsatalaboriosa"
        val manipulator = ImgurUrlManipulator(buildMockRepository("PlayfulIckyApisdorsatalaboriosa"))
        val recipe = RedditGifRecipe(url, "fake", ImageType.VIDEO, "fake", "fake", "fake", "fake", "")
        val testObserver = TestObserver<RedditGifRecipe>()
        manipulator.modifyRedditItem(recipe).subscribe(testObserver)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        Assert.assertTrue(testObserver.values().size == 1)
        Assert.assertEquals(testObserver.values()[0].url, testUrl)
    }

    @Test fun testFailedLookup() {
        val url = "https://i.imgur.com/PlayfulIckyApisdorsatalaboriosa"
        val manipulator = ImgurUrlManipulator(buildErrorRepository())
        val recipe = RedditGifRecipe(url, "fake", ImageType.VIDEO, "fake", "fake", "fake", "fake", "")
        val testObserver = TestObserver<RedditGifRecipe>()
        manipulator.modifyRedditItem(recipe).subscribe(testObserver)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        Assert.assertTrue(testObserver.values().size == 1)
        Assert.assertEquals(testObserver.values()[0], recipe) // we should get back the same item.
    }

    private fun buildEmptyRepository(): ImgurRepository {
        return object: ImgurRepository {
            override fun getImageInfo(imageId: String): Observable<ImgurPost> = Observable.empty()
        }
    }

    private fun buildMockRepository(id: String): ImgurRepository {
        return object: ImgurRepository {
            override fun getImageInfo(imageId: String): Observable<ImgurPost> {
                Assert.assertEquals("id != imageid", id, imageId)
                return Observable.just(ImgurPost(testUrl))
            }
        }
    }

    private fun buildErrorRepository(): ImgurRepository {
        return object: ImgurRepository {
            override fun getImageInfo(imageId: String): Observable<ImgurPost> =
                    Observable.error(RuntimeException(errorMessage))
        }
    }
}