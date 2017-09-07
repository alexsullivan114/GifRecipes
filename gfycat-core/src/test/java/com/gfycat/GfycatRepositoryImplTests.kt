package com.gfycat

import com.alexsullivan.logging.Logger
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Test
import retrofit2.Response

class GfycatRepositoryImplTests {
    val logger = object: Logger {
        override fun printLn(priority: Int, tag: String, msg: String) = -1
        override fun e(tag: String, msg: String, error: Throwable) {}
    }

    @Test
    fun happyPathTest() {
        val service = object: GfycatService {
            override fun getImage(gfycatId: String) = Observable.just(Response.success<GfycatPost>(GfycatPost("Test1")))
        }

        val repo = GfycatRepositoryImpl(service, logger)
        val testObserver = TestObserver<GfycatPost>()
        repo.getImageInfo("").subscribe(testObserver)
        Assert.assertTrue(testObserver.awaitTerminalEvent())
        Assert.assertEquals(1, testObserver.valueCount())
        Assert.assertEquals("Test1", testObserver.values()[0].mp4)
    }

    @Test
    fun testErrorConsumed() {
        val service = object: GfycatService {
            val responseBody = ResponseBody.create(MediaType.parse("application/json"), "")
            override fun getImage(gfycatId: String) = Observable.just(Response.error<GfycatPost>(404, responseBody))
        }

        val repo = GfycatRepositoryImpl(service, logger)
        val testObserver = TestObserver<GfycatPost>()
        repo.getImageInfo("").subscribe(testObserver)
        Assert.assertTrue(testObserver.awaitTerminalEvent())
        testObserver.assertComplete()
        Assert.assertEquals(0, testObserver.valueCount())
    }
}