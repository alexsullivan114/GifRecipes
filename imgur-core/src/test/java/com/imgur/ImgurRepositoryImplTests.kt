package com.imgur

import com.alexsullivan.logging.Logger
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Test
import retrofit2.Response

class ImgurRepositoryImplTests {
    val logger = object: Logger {
        override fun printLn(priority: Int, tag: String, msg: String) = -1
        override fun e(tag: String, msg: String, error: Throwable) {}
    }

    @Test fun happyPathTest() {
        val service = object: ImgurService {
            override fun getImage(imageId: String) = Observable.just(Response.success<ImgurPost>(ImgurPost("Test1")))
        }

        val repo = ImgurRepositoryImpl(service, logger)
        val testObserver = TestObserver<ImgurPost>()
        repo.getImageInfo("").subscribe(testObserver)
        Assert.assertTrue(testObserver.awaitTerminalEvent())
        Assert.assertEquals(1, testObserver.valueCount())
        Assert.assertEquals("Test1", testObserver.values()[0].mp4)
    }

    @Test fun testErrorConsumed() {
        val service = object: ImgurService {
            val responseBody = ResponseBody.create(MediaType.parse("application/json"), "")
            override fun getImage(imageId: String) = Observable.just(Response.error<ImgurPost>(404, responseBody))
        }

        val repo = ImgurRepositoryImpl(service, logger)
        val testObserver = TestObserver<ImgurPost>()
        repo.getImageInfo("").subscribe(testObserver)
        Assert.assertTrue(testObserver.awaitTerminalEvent())
        testObserver.assertComplete()
        Assert.assertEquals(0, testObserver.valueCount())
    }
}